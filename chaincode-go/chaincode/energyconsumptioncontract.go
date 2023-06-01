package chaincode

import (
	"encoding/json"
	"fmt"

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
	ID    string `json:"ID"`
	Name  string `json:"Name"`
	Value string `json:"Value"`
}

// InitLedger adds a base set of energyConsumptions to the ledger
func (s *EnergyConsumptionContract) InitLedger(ctx contractapi.TransactionContextInterface) error {
	energyConsumptions := []EnergyConsumption{
		{ID: "att1", Name: "a1", Value: "v1"},
		{ID: "att2", Name: "a2", Value: "v2"},
		{ID: "attr3", Name: "a3", Value: "v3"},
		{ID: "att4", Name: "a4", Value: "v4"},
		{ID: "att5", Name: "a5", Value: "v5"},
		{ID: "att6", Name: "a6", Value: "v6"},
		{ID: "att7", Name: "a1", Value: "v2"},
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
func (s *EnergyConsumptionContract) CreateEnergyConsumption(ctx contractapi.TransactionContextInterface, id string, name string, value string) error {
	exists, err := s.EnergyConsumptionExists(ctx, id)
	if err != nil {
		return err
	}
	if exists {
		return fmt.Errorf("the energyConsumption %s already exists", id)
	}

	energyConsumption := EnergyConsumption{
		ID:    id,
		Name:  name,
		Value: value,
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
func (s *EnergyConsumptionContract) UpdateEnergyConsumption(ctx contractapi.TransactionContextInterface, id string, name string, value string) error {
	exists, err := s.EnergyConsumptionExists(ctx, id)
	if err != nil {
		return err
	}
	if !exists {
		return fmt.Errorf("the energyConsumption %s does not exist", id)
	}

	// overwriting original energyConsumption with new energyConsumption
	energyConsumption := EnergyConsumption{
		ID:    id,
		Name:  name,
		Value: value,
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
	resultsIterator, err := ctx.GetStub().GetStateByRange("", "")
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
