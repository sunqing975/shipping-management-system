package chaincode

import (
	"encoding/json"
	"fmt"
	"math"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

// SmartContract provides functions for managing an ECPolicy
type ECPolicyContract struct {
	contractapi.Contract
}

// ECPolicy describes basic details of what makes up a simple ecPolicy
// Insert struct field in alphabetic order => to achieve determinism across languages
// golang keeps the order when marshal to json but doesn't order automatically
type ECPolicy struct {
	ID     string `json:"ID"`
	EcId   string `json:"EcId"`
	Policy string `json:"Policy"`
}

// InitLedger adds a base set of ecPolicys to the ledger
func (s *ECPolicyContract) InitLedger(ctx contractapi.TransactionContextInterface) error {
	ecPolicys := []ECPolicy{
		{ID: "policy1", EcId: "ec1", Policy: "att1 att2 att3 2of3 att4 1of2"},
		{ID: "policy2", EcId: "ec2", Policy: "att1 att2 att3 2of3 att4 1of2"},
		{ID: "policy3", EcId: "ec3", Policy: "att1 att2 att3 2of3 att4 1of2"},
		{ID: "policy4", EcId: "ec4", Policy: "att1 att2 att3 2of3 att4 1of2"},
		{ID: "policy5", EcId: "ec5", Policy: "att1 att2 att3 2of3 att4 1of2"},
		{ID: "policy6", EcId: "ec6", Policy: "att1 att2 att3 2of3 att4 1of2"},
	}

	for _, ecPolicy := range ecPolicys {
		ecPolicyJSON, err := json.Marshal(ecPolicy)
		if err != nil {
			return err
		}

		err = ctx.GetStub().PutState(ecPolicy.ID, ecPolicyJSON)
		if err != nil {
			return fmt.Errorf("failed to put to world state. %v", err)
		}
	}

	return nil
}

// CreateECPolicy issues a new ecPolicy to the world state with given details.
func (s *ECPolicyContract) CreateECPolicy(ctx contractapi.TransactionContextInterface,
	id string, ecId string, policy string) error {
	exists, err := s.ECPolicyExists(ctx, id)
	if err != nil {
		return err
	}
	if exists {
		return fmt.Errorf("the ecPolicy %s already exists", id)
	}

	ecPolicy := ECPolicy{
		ID:     id,
		EcId:   ecId,
		Policy: policy,
	}
	ecPolicyJSON, err := json.Marshal(ecPolicy)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState(id, ecPolicyJSON)
}

// ReadECPolicy returns the ecPolicy stored in the world state with given id.
func (s *ECPolicyContract) ReadECPolicy(ctx contractapi.TransactionContextInterface, id string) (*ECPolicy, error) {
	ecPolicyJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return nil, fmt.Errorf("failed to read from world state: %v", err)
	}
	if ecPolicyJSON == nil {
		return nil, fmt.Errorf("the ecPolicy %s does not exist", id)
	}

	var ecPolicy ECPolicy
	err = json.Unmarshal(ecPolicyJSON, &ecPolicy)
	if err != nil {
		return nil, err
	}

	return &ecPolicy, nil
}

// UpdateECPolicy updates an existing ecPolicy in the world state with provided parameters.
func (s *ECPolicyContract) UpdateECPolicy(ctx contractapi.TransactionContextInterface,
	id string, ecId string, policy string) error {
	exists, err := s.ECPolicyExists(ctx, id)
	if err != nil {
		return err
	}
	if !exists {
		return fmt.Errorf("the ecPolicy %s does not exist", id)
	}

	// overwriting original ecPolicy with new ecPolicy
	ecPolicy := ECPolicy{
		ID:     id,
		EcId:   ecId,
		Policy: policy,
	}
	ecPolicyJSON, err := json.Marshal(ecPolicy)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState(id, ecPolicyJSON)
}

// DeleteECPolicy deletes an given ecPolicy from the world state.
func (s *ECPolicyContract) DeleteECPolicy(ctx contractapi.TransactionContextInterface, id string) error {
	exists, err := s.ECPolicyExists(ctx, id)
	if err != nil {
		return err
	}
	if !exists {
		return fmt.Errorf("the ecPolicy %s does not exist", id)
	}

	return ctx.GetStub().DelState(id)
}

// ECPolicyExists returns true when ecPolicy with given ID exists in world state
func (s *ECPolicyContract) ECPolicyExists(ctx contractapi.TransactionContextInterface, id string) (bool, error) {
	ecPolicyJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return false, fmt.Errorf("failed to read from world state: %v", err)
	}

	return ecPolicyJSON != nil, nil
}

// GetAllECPolicys returns all ecPolicys found in world state
func (s *ECPolicyContract) GetAllECPolicys(ctx contractapi.TransactionContextInterface) ([]*ECPolicy, error) {
	// range query with empty string for startKey and endKey does an
	// open-ended query of all ecPolicys in the chaincode namespace.
	resultsIterator, err := ctx.GetStub().GetStateByRange("policy1", "policy"+string(math.MaxInt64))
	if err != nil {
		return nil, err
	}
	defer resultsIterator.Close()

	var ecPolicys []*ECPolicy
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}

		var ecPolicy ECPolicy
		err = json.Unmarshal(queryResponse.Value, &ecPolicy)
		if err != nil {
			return nil, err
		}
		ecPolicys = append(ecPolicys, &ecPolicy)
	}

	return ecPolicys, nil
}
