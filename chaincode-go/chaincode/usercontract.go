package chaincode

import (
	"encoding/json"
	"fmt"
	"math"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

// SmartContract provides functions for managing an User
type UserContract struct {
	contractapi.Contract
}

// User describes basic details of what makes up a simple user
// Insert struct field in alphabetic order => to achieve determinism across languages
// golang keeps the order when marshal to json but doesn't order automatically
type User struct {
	ID       string `json:"ID"`
	Username string `json:"Username"`
	Role     int    `json:"Role"`
}

// InitLedger adds a base set of users to the ledger
func (s *UserContract) InitLedger(ctx contractapi.TransactionContextInterface) error {
	users := []User{
		{ID: "user1", Username: "admin", Role: 2},
		{ID: "user2", Username: "tom", Role: 1},
		{ID: "user3", Username: "jack", Role: 1},
		{ID: "user4", Username: "ruth", Role: 1},
		{ID: "user5", Username: "jerry", Role: 1},
		{ID: "user6", Username: "david", Role: 1},
	}

	for _, user := range users {
		userJSON, err := json.Marshal(user)
		if err != nil {
			return err
		}

		err = ctx.GetStub().PutState(user.ID, userJSON)
		if err != nil {
			return fmt.Errorf("failed to put to world state. %v", err)
		}
	}

	return nil
}

// CreateUser issues a new user to the world state with given details.
func (s *UserContract) CreateUser(ctx contractapi.TransactionContextInterface, id string, username string, role int) error {
	exists, err := s.UserExists(ctx, id)
	if err != nil {
		return err
	}
	if exists {
		return fmt.Errorf("the user %s already exists", id)
	}

	user := User{
		ID:       id,
		Username: username,
		Role:     role,
	}
	userJSON, err := json.Marshal(user)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState(id, userJSON)
}

// ReadUser returns the user stored in the world state with given id.
func (s *UserContract) ReadUser(ctx contractapi.TransactionContextInterface, id string) (*User, error) {
	userJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return nil, fmt.Errorf("failed to read from world state: %v", err)
	}
	if userJSON == nil {
		return nil, fmt.Errorf("the user %s does not exist", id)
	}

	var user User
	err = json.Unmarshal(userJSON, &user)
	if err != nil {
		return nil, err
	}

	return &user, nil
}

// UpdateUser updates an existing user in the world state with provided parameters.
func (s *UserContract) UpdateUser(ctx contractapi.TransactionContextInterface, id string, username string, role int) error {
	exists, err := s.UserExists(ctx, id)
	if err != nil {
		return err
	}
	if !exists {
		return fmt.Errorf("the user %s does not exist", id)
	}

	// overwriting original user with new user
	user := User{
		ID:       id,
		Username: username,
		Role:     role,
	}
	userJSON, err := json.Marshal(user)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState(id, userJSON)
}

// DeleteUser deletes an given user from the world state.
func (s *UserContract) DeleteUser(ctx contractapi.TransactionContextInterface, id string) error {
	exists, err := s.UserExists(ctx, id)
	if err != nil {
		return err
	}
	if !exists {
		return fmt.Errorf("the user %s does not exist", id)
	}

	return ctx.GetStub().DelState(id)
}

// UserExists returns true when user with given ID exists in world state
func (s *UserContract) UserExists(ctx contractapi.TransactionContextInterface, id string) (bool, error) {
	userJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return false, fmt.Errorf("failed to read from world state: %v", err)
	}

	return userJSON != nil, nil
}

// GetAllUsers returns all users found in world state
func (s *UserContract) GetAllUsers(ctx contractapi.TransactionContextInterface) ([]*User, error) {
	// range query with empty string for startKey and endKey does an
	// open-ended query of all users in the chaincode namespace.

	resultsIterator, err := ctx.GetStub().GetStateByRange("user1", "user"+string(math.MaxInt64))
	if err != nil {
		return nil, err
	}
	defer resultsIterator.Close()

	var users []*User
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}

		var user User
		err = json.Unmarshal(queryResponse.Value, &user)
		if err != nil {
			return nil, err
		}
		users = append(users, &user)
	}

	return users, nil
}
