/*
 * 版权所有:杭州火图科技有限公司
 * 地址:浙江省杭州市滨江区西兴街道阡陌路智慧E谷B幢4楼在地图中查看
 *
 * (c) Copyright Hangzhou Hot Technology Co., Ltd.
 * Floor 4,Block B,Wisdom E Valley,Qianmo Road,Binjiang District
 * 2013-2016. All rights reserved.
 */

package com.huotu.mallutils.service.entity.config;

import com.huotu.mallutils.service.ienum.DeliveryType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

/**
 * 运费模板实体
 * Created by allan on 7/7/16.
 */
@Entity
@Table(name = "Mall_Freight_Template")
@Setter
@Getter
public class FreightTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;
    /**
     * 模板名称
     */
    @Column(name = "Name")
    private String name;
    /**
     * 是否包邮
     * 0:买家承担运费
     * 1:卖家承担运费
     */
    @Column(name = "Is_ShippingFree")
    private boolean isShippingFree;
    /**
     * 计价方式
     * 0:按件计价
     * 1:按重计价
     */
    @Column(name = "Valuation_Way")
    private int valuationWay;
    @Column(name = "Delivery_Type")
    private DeliveryType deliveryType;
    /**
     * 几件内或者多少重内,根据计价方式
     */
    @Column(name = "First_Item")
    private double firstItem;
    /**
     * 对应firstItem的运费
     */
    @Column(name = "First_Freight")
    private double firstFreight;
    /**
     * 每增加几件或者多少重,根据计价方式
     */
    @Column(name = "Next_Item")
    private double nextItem;
    /**
     * 对应nextItem的运费
     */
    @Column(name = "Next_Freight")
    private double nextFreight;
    /**
     * 商户ID
     */
    @Column(name = "Customer_Id")
    private int customerId;
    @Column(name = "Is_Default")
    private boolean isDefault;
    /**
     * 模板说明
     */
    @Column(name = "Template_Desc")
    private String description;
    @OneToMany(mappedBy = "freightTemplate", orphanRemoval = true, cascade = {CascadeType.PERSIST})
    private List<DesignatedAreaTemplate> designatedAreaTemplates;
}
