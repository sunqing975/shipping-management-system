package chaincode

import (
	"encoding/json"
	"fmt"
	"math"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

// SmartContract provides functions for managing an EnergyConsumption
type EnergyConsumptionContract struct {
	contractapi.Contract
}

// EnergyConsumption describes basic details of what makes up a simple energyConsumption
// Insert struct field in alphabetic order => to achieve determinism across languages
// golang keeps the order when marshal to json but doesn't order automatically
type EnergyConsumption struct {
	ID                  string  `json:"ID"`
	OrgCode             int     `json:"OrgCode"`
	ShipId              int     `json:"ShipId"`
	ShipName            string  `json:"ShipName"`
	ShipType            string  `json:"ShipType"`
	ShipSize            float64 `json:"ShipSize"`
	EnergyType          float64 `json:"EnergyType"`
	EnergyName          string  `json:"EnergyName"`
	ConsumeQuantity     float64 `json:"ConsumeQuantity"`
	ConsumeQuantityUnit string  `json:"ConsumeQuantityUnit"`
	OperatorId          string  `json:"OperatorId"`
	StartTime           int64   `json:"StartTime"`
	EndTime             int64   `json:"EndTime"`
}

// InitLedger adds a base set of energyConsumptions to the ledger
func (s *EnergyConsumptionContract) InitLedger(ctx contractapi.TransactionContextInterface) error {
	energyConsumptions := []EnergyConsumption{
		{ID: "ec1", OrgCode: 100, ShipId: 1, ShipName: "ship1", ShipType: "large",
			ShipSize: 53.5, EnergyType: 0.0, EnergyName: "岸电", ConsumeQuantity: 12.5,
			ConsumeQuantityUnit: "kw.h", OperatorId: "user1", StartTime: 1684422876, EndTime: 1684422901},
		{ID: "ec2", OrgCode: 100, ShipId: 2, ShipName: "ship2", ShipType: "small",
			ShipSize: 13.5, EnergyType: 0.2, EnergyName: "柴油/汽油（MDO/MGO", ConsumeQuantity: 4.0,
			ConsumeQuantityUnit: "吨", OperatorId: "user1", StartTime: 1684422876, EndTime: 1684422901},
		{ID: "ec3", OrgCode: 100, ShipId: 3, ShipName: "ship3", ShipType: "medium",
			ShipSize: 33.5, EnergyType: 2.2, EnergyName: "轻燃油（LFO） 硫含量高于0.1% m/m，但不高于0.5% m/m", ConsumeQuantity: 181.19,
			ConsumeQuantityUnit: "吨", OperatorId: "user2", StartTime: 1684422876, EndTime: 1684422901},
		{ID: "ec4", OrgCode: 100, ShipId: 4, ShipName: "ship4", ShipType: "large",
			ShipSize: 73.6, EnergyType: 0.2, EnergyName: "岸电", ConsumeQuantity: 12.5,
			ConsumeQuantityUnit: "kw.h", OperatorId: "user2", StartTime: 1684422876, EndTime: 1684422901},
		{ID: "ec5", OrgCode: 100, ShipId: 5, ShipName: "ship5", ShipType: "medium",
			ShipSize: 43.6, EnergyType: 0.2, EnergyName: "岸电", ConsumeQuantity: 12.5,
			ConsumeQuantityUnit: "kw.h", OperatorId: "user3", StartTime: 1684422876, EndTime: 1684422901},
		{ID: "ec6", OrgCode: 100, ShipId: 6, ShipName: "ship5", ShipType: "large",
			ShipSize: 63.5, EnergyType: 0.2, EnergyName: "重燃油（HFO） 硫含量高于0.5% m/m\"", ConsumeQuantity: 519.75,
			ConsumeQuantityUnit: "吨", OperatorId: "user3", StartTime: 1684422876, EndTime: 1684422901},
	}

	for _, energyConsumption := range energyConsumptions {
		energyConsumptionJSON, err := json.Marshal(energyConsumption)
		if err != nil {
			return err
		}

		err = ctx.GetStub().PutState(energyConsumption.ID, energyConsumptionJSON)
		if err != nil {
			return fmt.Errorf("failed to put to world state. %v", err)
		}
	}

	return nil
}

// CreateEnergyConsumption issues a new energyConsumption to the world state with given details.
func (s *EnergyConsumptionContract) CreateEnergyConsumption(ctx contractapi.TransactionContextInterface, id string,
	orgCode int, shipId int, shipName string, shipType string, shipSize float64, energyType float64, energyName string,
	consumeQuantity float64, consumeQuantityUnit string, operatorId string, startTime int64, endTime int64) error {
	exists, err := s.EnergyConsumptionExists(ctx, id)
	if err != nil {
		return err
	}
	if exists {
		return fmt.Errorf("the energyConsumption %s already exists", id)
	}

	energyConsumption := EnergyConsumption{
		ID:                  id,
		OrgCode:             orgCode,
		ShipId:              shipId,
		ShipName:            shipName,
		ShipType:            shipType,
		ShipSize:            shipSize,
		EnergyType:          energyType,
		EnergyName:          energyName,
		ConsumeQuantity:     consumeQuantity,
		ConsumeQuantityUnit: consumeQuantityUnit,
		OperatorId:          operatorId,
		StartTime:           startTime,
		EndTime:             endTime,
	}
	energyConsumptionJSON, err := json.Marshal(energyConsumption)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState(id, energyConsumptionJSON)
}

// ReadEnergyConsumption returns the energyConsumption stored in the world state with given id.
func (s *EnergyConsumptionContract) ReadEnergyConsumption(ctx contractapi.TransactionContextInterface, id string) (*EnergyConsumption, error) {
	energyConsumptionJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return nil, fmt.Errorf("failed to read from world state: %v", err)
	}
	if energyConsumptionJSON == nil {
		return nil, fmt.Errorf("the energyConsumption %s does not exist", id)
	}

	var energyConsumption EnergyConsumption
	err = json.Unmarshal(energyConsumptionJSON, &energyConsumption)
	if err != nil {
		return nil, err
	}

	return &energyConsumption, nil
}

// UpdateEnergyConsumption updates an existing energyConsumption in the world state with provided parameters.
func (s *EnergyConsumptionContract) UpdateEnergyConsumption(ctx contractapi.TransactionContextInterface,
	id string,
	orgCode int, shipId int, shipName string, shipType string, shipSize float64, energyType float64, energyName string,
	consumeQuantity float64, consumeQuantityUnit string, operatorId string, startTime int64, endTime int64) error {
	exists, err := s.EnergyConsumptionExists(ctx, id)
	if err != nil {
		return err
	}
	if !exists {
		return fmt.Errorf("the energyConsumption %s does not exist", id)
	}

	// overwriting original energyConsumption with new energyConsumption
	energyConsumption := EnergyConsumption{
		ID:                  id,
		OrgCode:             orgCode,
		ShipId:              shipId,
		ShipName:            shipName,
		ShipType:            shipType,
		ShipSize:            shipSize,
		EnergyType:          energyType,
		EnergyName:          energyName,
		ConsumeQuantity:     consumeQuantity,
		ConsumeQuantityUnit: consumeQuantityUnit,
		OperatorId:          operatorId,
		StartTime:           startTime,
		EndTime:             endTime,
	}
	energyConsumptionJSON, err := json.Marshal(energyConsumption)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState(id, energyConsumptionJSON)
}

// DeleteEnergyConsumption deletes an given energyConsumption from the world state.
func (s *EnergyConsumptionContract) DeleteEnergyConsumption(ctx contractapi.TransactionContextInterface, id string) error {
	exists, err := s.EnergyConsumptionExists(ctx, id)
	if err != nil {
		return err
	}
	if !exists {
		return fmt.Errorf("the energyConsumption %s does not exist", id)
	}

	return ctx.GetStub().DelState(id)
}

// EnergyConsumptionExists returns true when energyConsumption with given ID exists in world state
func (s *EnergyConsumptionContract) EnergyConsumptionExists(ctx contractapi.TransactionContextInterface, id string) (bool, error) {
	energyConsumptionJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return false, fmt.Errorf("failed to read from world state: %v", err)
	}

	return energyConsumptionJSON != nil, nil
}

// GetAllEnergyConsumptions returns all energyConsumptions found in world state
func (s *EnergyConsumptionContract) GetAllEnergyConsumptions(ctx contractapi.TransactionContextInterface) ([]*EnergyConsumption, error) {
	// range query with empty string for startKey and endKey does an
	// open-ended query of all energyConsumptions in the chaincode namespace.
	resultsIterator, err := ctx.GetStub().GetStateByRange("ec1", "ec"+string(math.MaxInt64))
	if err != nil {
		return nil, err
	}
	defer resultsIterator.Close()

	var energyConsumptions []*EnergyConsumption
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}

		var energyConsumption EnergyConsumption
		err = json.Unmarshal(queryResponse.Value, &energyConsumption)
		if err != nil {
			return nil, err
		}
		energyConsumptions = append(energyConsumptions, &energyConsumption)
	}

	return energyConsumptions, nil
}
