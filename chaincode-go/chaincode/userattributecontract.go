package chaincode

import (
	"encoding/json"
	"fmt"
	"math"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

// SmartContract provides functions for managing an UserAttribute
type UserAttributeContract struct {
	contractapi.Contract
}

// UserAttribute describes basic details of what makes up a simple ua
// Insert struct field in alphabetic order => to achieve determinism across languages
// golang keeps the order when marshal to json but doesn't order automatically
type UserAttribute struct {
	ID     string `json:"ID"`
	UserId string `json:"UserId"`
	AttId  string `json:"AttId"`
}

// InitLedger adds a base set of uas to the ledger
func (s *UserAttributeContract) InitLedger(ctx contractapi.TransactionContextInterface) error {
	uas := []UserAttribute{
		{ID: "ua1", UserId: "user2", AttId: "att1"},
		{ID: "ua2", UserId: "user2", AttId: "att2"},
		{ID: "ua3", UserId: "user2", AttId: "att3"},
		{ID: "ua4", UserId: "user3", AttId: "att2"},
		{ID: "ua5", UserId: "user3", AttId: "att4"},
		{ID: "ua6", UserId: "user4", AttId: "att6"},
	}

	for _, ua := range uas {
		uaJSON, err := json.Marshal(ua)
		if err != nil {
			return err
		}

		err = ctx.GetStub().PutState(ua.ID, uaJSON)
		if err != nil {
			return fmt.Errorf("failed to put to world state. %v", err)
		}
	}

	return nil
}

// CreateUserAttribute issues a new ua to the world state with given details.
func (s *UserAttributeContract) CreateUserAttribute(ctx contractapi.TransactionContextInterface, id string, userId string, attId string) error {
	exists, err := s.UserAttributeExists(ctx, id)
	if err != nil {
		return err
	}
	if exists {
		return fmt.Errorf("the ua %s already exists", id)
	}

	ua := UserAttribute{
		ID:     id,
		UserId: userId,
		AttId:  attId,
	}
	uaJSON, err := json.Marshal(ua)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState(id, uaJSON)
}

// ReadUserAttribute returns the ua stored in the world state with given id.
func (s *UserAttributeContract) ReadUserAttribute(ctx contractapi.TransactionContextInterface, id string) (*UserAttribute, error) {
	uaJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return nil, fmt.Errorf("failed to read from world state: %v", err)
	}
	if uaJSON == nil {
		return nil, fmt.Errorf("the ua %s does not exist", id)
	}

	var ua UserAttribute
	err = json.Unmarshal(uaJSON, &ua)
	if err != nil {
		return nil, err
	}

	return &ua, nil
}

// UpdateUserAttribute updates an existing ua in the world state with provided parameters.
func (s *UserAttributeContract) UpdateUserAttribute(ctx contractapi.TransactionContextInterface, id string, userId string, attId string) error {
	exists, err := s.UserAttributeExists(ctx, id)
	if err != nil {
		return err
	}
	if !exists {
		return fmt.Errorf("the ua %s does not exist", id)
	}

	// overwriting original ua with new ua
	ua := UserAttribute{
		ID:     id,
		UserId: userId,
		AttId:  attId,
	}
	uaJSON, err := json.Marshal(ua)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState(id, uaJSON)
}

// DeleteUserAttribute deletes an given ua from the world state.
func (s *UserAttributeContract) DeleteUserAttribute(ctx contractapi.TransactionContextInterface, id string) error {
	exists, err := s.UserAttributeExists(ctx, id)
	if err != nil {
		return err
	}
	if !exists {
		return fmt.Errorf("the ua %s does not exist", id)
	}

	return ctx.GetStub().DelState(id)
}

// UserAttributeExists returns true when ua with given ID exists in world state
func (s *UserAttributeContract) UserAttributeExists(ctx contractapi.TransactionContextInterface, id string) (bool, error) {
	uaJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return false, fmt.Errorf("failed to read from world state: %v", err)
	}

	return uaJSON != nil, nil
}

// GetAllUserAttributes returns all uas found in world state
func (s *UserAttributeContract) GetAllUserAttributes(ctx contractapi.TransactionContextInterface) ([]*UserAttribute, error) {
	// range query with empty string for startKey and endKey does an
	// open-ended query of all uas in the chaincode namespace.
	resultsIterator, err := ctx.GetStub().GetStateByRange("ua1", "ua"+string(math.MaxInt64))
	if err != nil {
		return nil, err
	}
	defer resultsIterator.Close()

	var uas []*UserAttribute
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}

		var ua UserAttribute
		err = json.Unmarshal(queryResponse.Value, &ua)
		if err != nil {
			return nil, err
		}
		uas = append(uas, &ua)
	}

	return uas, nil
}
