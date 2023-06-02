package chaincode

import (
	"encoding/json"
	"fmt"
	"math"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

// SmartContract provides functions for managing an Attribute
type AttributeContract struct {
	contractapi.Contract
}

// Attribute describes basic details of what makes up a simple attribute
// Insert struct field in alphabetic order => to achieve determinism across languages
// golang keeps the order when marshal to json but doesn't order automatically
type Attribute struct {
	ID    string `json:"ID"`
	Name  string `json:"Name"`
	Value string `json:"Value"`
}

// InitLedger adds a base set of attributes to the ledger
func (s *AttributeContract) InitLedger(ctx contractapi.TransactionContextInterface) error {
	attributes := []Attribute{
		{ID: "att1", Name: "a1", Value: "v1"},
		{ID: "att2", Name: "a2", Value: "v2"},
		{ID: "att3", Name: "a3", Value: "v3"},
		{ID: "att4", Name: "a4", Value: "v4"},
		{ID: "att5", Name: "a5", Value: "v5"},
		{ID: "att6", Name: "a6", Value: "v6"},
		{ID: "att7", Name: "a1", Value: "v2"},
	}

	for _, attribute := range attributes {
		attributeJSON, err := json.Marshal(attribute)
		if err != nil {
			return err
		}

		err = ctx.GetStub().PutState(attribute.ID, attributeJSON)
		if err != nil {
			return fmt.Errorf("failed to put to world state. %v", err)
		}
	}

	return nil
}

// CreateAttribute issues a new attribute to the world state with given details.
func (s *AttributeContract) CreateAttribute(ctx contractapi.TransactionContextInterface, id string, name string, value string) error {
	exists, err := s.AttributeExists(ctx, id)
	if err != nil {
		return err
	}
	if exists {
		return fmt.Errorf("the attribute %s already exists", id)
	}

	attribute := Attribute{
		ID:    id,
		Name:  name,
		Value: value,
	}
	attributeJSON, err := json.Marshal(attribute)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState(id, attributeJSON)
}

// ReadAttribute returns the attribute stored in the world state with given id.
func (s *AttributeContract) ReadAttribute(ctx contractapi.TransactionContextInterface, id string) (*Attribute, error) {
	attributeJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return nil, fmt.Errorf("failed to read from world state: %v", err)
	}
	if attributeJSON == nil {
		return nil, fmt.Errorf("the attribute %s does not exist", id)
	}

	var attribute Attribute
	err = json.Unmarshal(attributeJSON, &attribute)
	if err != nil {
		return nil, err
	}

	return &attribute, nil
}

// UpdateAttribute updates an existing attribute in the world state with provided parameters.
func (s *AttributeContract) UpdateAttribute(ctx contractapi.TransactionContextInterface, id string, name string, value string) error {
	exists, err := s.AttributeExists(ctx, id)
	if err != nil {
		return err
	}
	if !exists {
		return fmt.Errorf("the attribute %s does not exist", id)
	}

	// overwriting original attribute with new attribute
	attribute := Attribute{
		ID:    id,
		Name:  name,
		Value: value,
	}
	attributeJSON, err := json.Marshal(attribute)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState(id, attributeJSON)
}

// DeleteAttribute deletes an given attribute from the world state.
func (s *AttributeContract) DeleteAttribute(ctx contractapi.TransactionContextInterface, id string) error {
	exists, err := s.AttributeExists(ctx, id)
	if err != nil {
		return err
	}
	if !exists {
		return fmt.Errorf("the attribute %s does not exist", id)
	}

	return ctx.GetStub().DelState(id)
}

// AttributeExists returns true when attribute with given ID exists in world state
func (s *AttributeContract) AttributeExists(ctx contractapi.TransactionContextInterface, id string) (bool, error) {
	attributeJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return false, fmt.Errorf("failed to read from world state: %v", err)
	}

	return attributeJSON != nil, nil
}

// GetAllAttributes returns all attributes found in world state
func (s *AttributeContract) GetAllAttributes(ctx contractapi.TransactionContextInterface) ([]*Attribute, error) {
	// range query with empty string for startKey and endKey does an
	// open-ended query of all attributes in the chaincode namespace.
	resultsIterator, err := ctx.GetStub().GetStateByRange("att1", "att"+string(math.MaxInt64))
	if err != nil {
		return nil, err
	}
	defer resultsIterator.Close()

	var attributes []*Attribute
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}

		var attribute Attribute
		err = json.Unmarshal(queryResponse.Value, &attribute)
		if err != nil {
			return nil, err
		}
		attributes = append(attributes, &attribute)
	}

	return attributes, nil
}
