package com.huotu.mallutils.service.service.good.impl;

import com.alibaba.fastjson.JSON;
import com.huotu.mallutils.service.entity.good.Good;
import com.huotu.mallutils.service.entity.good.GoodLvPrice;
import com.huotu.mallutils.service.entity.good.Product;
import com.huotu.mallutils.service.entity.user.Level;
import com.huotu.mallutils.service.model.PriceLevelDesc;
import com.huotu.mallutils.service.repository.good.GoodRepository;
import com.huotu.mallutils.service.service.good.GoodService;
import com.huotu.mallutils.service.service.good.ProductService;
import com.huotu.mallutils.service.service.user.LevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by allan on 5/16/16.
 */
@Service
public class GoodServiceImpl implements GoodService {
    @Autowired
    private GoodRepository goodRepository;
    @Autowired
    private LevelService levelService;
    @Autowired
    private ProductService productService;

    @Override
    public Good save(Good good) {
        return goodRepository.save(good);
    }

    @Override
    @Transactional
    public void batchSetUserPrice(Map<Integer, String> levelsToSet, List<Good> goods, int customerId) throws ScriptException {
        ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");

        List<Level> levels = levelService.findByCustomerId(296);
        for (Good good : goods) {
            double minPrice = 0, maxPrice = 0;
            for (Product product : good.getProducts()) {
                String userPriceInfo = "";
                if (product.getGoodLvPriceList() == null || product.getGoodLvPriceList().size() == 0) {
                    List<GoodLvPrice> goodLvPriceList = new ArrayList<>();
                    for (Level level : levels) {
                        String eval = levelsToSet.get(level.getId());
                        double resultPrice = -1; //默认的价格
                        if (!StringUtils.isEmpty(eval)) {
                            resultPrice = getResultPrice(eval, product.getCost(), product.getPrice(), product.getMarketPrice(), scriptEngine);
                        }
                        GoodLvPrice goodLvPrice = new GoodLvPrice();
                        goodLvPrice.setPrice(resultPrice);
                        goodLvPrice.setCustomerId(good.getCustomerId());
                        goodLvPrice.setGoodsId(good.getGoodId());
                        goodLvPrice.setLevelId(level.getId());
                        goodLvPriceList.add(goodLvPrice);
                        //货品冗余字段

                        userPriceInfo += level.getId() + ":" + resultPrice + ":" + goodLvPrice.getMaxIntegral() + "|";

                        if (minPrice == 0 || minPrice >= resultPrice) {
                            minPrice = resultPrice;
                        }
                        if (resultPrice >= maxPrice) {
                            maxPrice = resultPrice;
                        }
                    }
                    product.setGoodLvPriceList(goodLvPriceList);
                } else {
                    for (GoodLvPrice goodLvPrice : product.getGoodLvPriceList()) {
                        String eval = levelsToSet.get(goodLvPrice.getLevelId());
                        double resultPrice = -1;
                        if (!StringUtils.isEmpty(eval)) {
                            resultPrice = getResultPrice(eval, product.getCost(), product.getPrice(), product.getMarketPrice(), scriptEngine);
                            goodLvPrice.setPrice(resultPrice);
                        }
                        //货品冗余字段
                        userPriceInfo += goodLvPrice.getLevelId() + ":" + resultPrice + ":" + goodLvPrice.getMaxIntegral() + "|";

                        if (minPrice == 0 || minPrice >= resultPrice) {
                            minPrice = resultPrice;
                        }
                        if (resultPrice >= maxPrice) {
                            maxPrice = resultPrice;
                        }
                    }
                }
                //处理冗余字段
                product.setUserPriceInfo(userPriceInfo.substring(0, userPriceInfo.length() - 1));
            }

            productService.batchSave(good.getProducts());
            //处理商品冗余字段
            if (StringUtils.isEmpty(good.getPriceLevelDesc())) {
                good.setPriceLevelDesc("[]");
            }
            List<PriceLevelDesc> priceLevelDescList = JSON.parseArray(good.getPriceLevelDesc(), PriceLevelDesc.class);
            if (priceLevelDescList.size() > 0) {
                for (PriceLevelDesc priceLevelDesc : priceLevelDescList) {
                    priceLevelDesc.setMinPrice(minPrice);
                    priceLevelDesc.setMaxPrice(maxPrice);
                }
            } else {
                priceLevelDescList = new ArrayList<>();
                for (Level level : levels) {
                    PriceLevelDesc priceLevelDesc = new PriceLevelDesc();
                    priceLevelDesc.setLevelId(level.getId());
                    priceLevelDesc.setMinPrice(minPrice);
                    priceLevelDesc.setMaxPrice(maxPrice);
                    priceLevelDescList.add(priceLevelDesc);
                }
            }
            good.setPriceLevelDesc(JSON.toJSONString(priceLevelDescList));
            goodRepository.save(good);
        }
    }

    @Override
    public List<Good> findByCatId(int catId) {
        return goodRepository.findByGoodCat_CatId(catId);
    }

    /**
     * 根据公式得到目标价格
     *
     * @param eval
     * @param cost        成本价
     * @param price       销售价
     * @param marketPrice 市场价
     * @return
     */
    private double getResultPrice(String eval, double cost, double price, double marketPrice, ScriptEngine scriptEngine) throws ScriptException {
        eval = eval.replaceAll("a", String.valueOf(cost));
        eval = eval.replaceAll("b", String.valueOf(marketPrice));
        eval = eval.replaceAll("c", String.valueOf(price));
        double resultPrice = Double.parseDouble(scriptEngine.eval(eval).toString());
        resultPrice = BigDecimal.valueOf(resultPrice).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(); //目标价格

        return resultPrice;
    }
}
