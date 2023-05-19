package com.shipping.entity;

/**
 * @className: com.shipping.entity.EnergyConsumption
 * @author: Superman
 * @create: 2023-05-17 21:39
 * @description: TODO
 */
public class EnergyConsumption {
    private String id;
    /**
     * 机构代码
     */
    private Integer orgCode;
    /**
     * 船只编号
     */
    private Integer shipId;
    /**
     * 船只名称
     */
    private String shipName;
    /**
     * 船只类型
     */
    private String shipType;
    /**
     * 船尺寸
     */
    private Double shipSize;
    /**
     * 能源类型
     */
    private Double energyType;
    /**
     * 能源名称
     */
    private String energyName;
    /**
     * 能耗数量
     */
    private Double consumeQuantity;

    /**
     * 能耗数量单位
     */
    private String consumeQuantityUnit;

    /**
     * 上传者ID
     */
    private String operatorId;
    /**
     * 开始时间
     */
    private Long startTime;
    /**
     * 结束时间
     */
    private Long endTime;

    public EnergyConsumption(String id, Integer orgCode, Integer shipId,
                             String shipName, String shipType,
                             Double shipSize, Double energyType,
                             String energyName, Double consumeQuantity,
                             String consumeQuantityUnit, String operatorId,
                             Long startTime, Long endTime) {
        this.id = id;
        this.orgCode = orgCode;
        this.shipId = shipId;
        this.shipName = shipName;
        this.shipType = shipType;
        this.shipSize = shipSize;
        this.energyType = energyType;
        this.energyName = energyName;
        this.consumeQuantity = consumeQuantity;
        this.consumeQuantityUnit = consumeQuantityUnit;
        this.operatorId = operatorId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(Integer orgCode) {
        this.orgCode = orgCode;
    }

    public Integer getShipId() {
        return shipId;
    }

    public void setShipId(Integer shipId) {
        this.shipId = shipId;
    }

    public void setConsumeQuantity(Double consumeQuantity) {
        this.consumeQuantity = consumeQuantity;
    }

    public String getShipName() {
        return shipName;
    }

    public void setShipName(String shipName) {
        this.shipName = shipName;
    }

    public String getShipType() {
        return shipType;
    }

    public void setShipType(String shipType) {
        this.shipType = shipType;
    }

    public Double getShipSize() {
        return shipSize;
    }

    public void setShipSize(Double shipSize) {
        this.shipSize = shipSize;
    }

    public Double getEnergyType() {
        return energyType;
    }

    public void setEnergyType(Double energyType) {
        this.energyType = energyType;
    }

    public String getEnergyName() {
        return energyName;
    }

    public void setEnergyName(String energyName) {
        this.energyName = energyName;
    }

    public Double getConsumeQuantity() {
        return consumeQuantity;
    }

    public String getConsumeQuantityUnit() {
        return consumeQuantityUnit;
    }

    public void setConsumeQuantityUnit(String consumeQuantityUnit) {
        this.consumeQuantityUnit = consumeQuantityUnit;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
}
