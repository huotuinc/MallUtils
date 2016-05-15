package com.huotu.mallutils.service.entity.user;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Created by allan on 5/16/16.
 */
@Entity
@Table(name = "Mall_UserLevel")
@Setter
@Getter
public class Level {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UL_ID")
    private Integer id;
    @Column(name = "UL_Level")
    private int level;
    @Column(name = "UL_LevelName")
    private String levelName;
    @Column(name = "UL_CustomerID")
    private int customerId;
    @Column(name = "UL_Type")
    private int type;
}
