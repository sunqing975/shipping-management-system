package com.shipping.entity;

/**
 * @className: com.shipping.entity.EnergyConsumption
 * @author: Superman
 * @create: 2023-05-17 21:39
 * @description: TODO
 */
public class EnergyConsumption {
    private String ID;
    /**
     * 机构代码
     */
    private Integer OrgCode;
    /**
     * 船只编号
     */
    private Integer ShipId;
    /**
     * 船只名称
     */
    private String ShipName;
    /**
     * 船只类型
     */
    private String ShipType;
    /**
     * 船尺寸
     */
    private Double ShipSize;
    /**
     * 能源类型
     */
    private Double EnergyType;
    /**
     * 能源名称
     */
    private String EnergyName;
    /**
     * 能耗数量
     */
    private Double ConsumeQuantity;

    /**
     * 能耗数量单位
     */
    private String ConsumeQuantityUnit;

    /**
     * 上传者ID
     */
    private String OperatorId;
    /**
     * 开始时间
     */
    private Long StartTime;
    /**
     * 结束时间
     */
    private Long EndTime;

    public EnergyConsumption(String ID, Integer orgCode, Integer shipId, String shipName, String shipType, Double shipSize, Double energyType, String energyName, Double consumeQuantity, String consumeQuantityUnit, String operatorId, Long startTime, Long endTime) {
        this.ID = ID;
        OrgCode = orgCode;
        ShipId = shipId;
        ShipName = shipName;
        ShipType = shipType;
        ShipSize = shipSize;
        EnergyType = energyType;
        EnergyName = energyName;
        ConsumeQuantity = consumeQuantity;
        ConsumeQuantityUnit = consumeQuantityUnit;
        OperatorId = operatorId;
        StartTime = startTime;
        EndTime = endTime;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Integer getOrgCode() {
        return OrgCode;
    }

    public void setOrgCode(Integer orgCode) {
        OrgCode = orgCode;
    }

    public Integer getShipId() {
        return ShipId;
    }

    public void setShipId(Integer shipId) {
        ShipId = shipId;
    }

    public String getShipName() {
        return ShipName;
    }

    public void setShipName(String shipName) {
        ShipName = shipName;
    }

    public String getShipType() {
        return ShipType;
    }

    public void setShipType(String shipType) {
        ShipType = shipType;
    }

    public Double getShipSize() {
        return ShipSize;
    }

    public void setShipSize(Double shipSize) {
        ShipSize = shipSize;
    }

    public Double getEnergyType() {
        return EnergyType;
    }

    public void setEnergyType(Double energyType) {
        EnergyType = energyType;
    }

    public String getEnergyName() {
        return EnergyName;
    }

    public void setEnergyName(String energyName) {
        EnergyName = energyName;
    }

    public Double getConsumeQuantity() {
        return ConsumeQuantity;
    }

    public void setConsumeQuantity(Double consumeQuantity) {
        ConsumeQuantity = consumeQuantity;
    }

    public String getConsumeQuantityUnit() {
        return ConsumeQuantityUnit;
    }

    public void setConsumeQuantityUnit(String consumeQuantityUnit) {
        ConsumeQuantityUnit = consumeQuantityUnit;
    }

    public String getOperatorId() {
        return OperatorId;
    }

    public void setOperatorId(String operatorId) {
        OperatorId = operatorId;
    }

    public Long getStartTime() {
        return StartTime;
    }

    public void setStartTime(Long startTime) {
        StartTime = startTime;
    }

    public Long getEndTime() {
        return EndTime;
    }

    public void setEndTime(Long endTime) {
        EndTime = endTime;
    }

    @Override
    public String toString() {
        return "EnergyConsumption{" +
                "ID='" + ID + '\'' +
                ", OrgCode=" + OrgCode +
                ", ShipId=" + ShipId +
                ", ShipName='" + ShipName + '\'' +
                ", ShipType='" + ShipType + '\'' +
                ", ShipSize=" + ShipSize +
                ", EnergyType=" + EnergyType +
                ", EnergyName='" + EnergyName + '\'' +
                ", ConsumeQuantity=" + ConsumeQuantity +
                ", ConsumeQuantityUnit='" + ConsumeQuantityUnit + '\'' +
                ", OperatorId='" + OperatorId + '\'' +
                ", StartTime=" + StartTime +
                ", EndTime=" + EndTime +
                '}';
    }
}
