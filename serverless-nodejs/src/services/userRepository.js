const { DynamoDBClient } = require('@aws-sdk/client-dynamodb');
const { DynamoDBDocumentClient, GetCommand, PutCommand, UpdateCommand, DeleteCommand, QueryCommand } = require('@aws-sdk/lib-dynamodb');
const { v4: uuidv4 } = require('uuid');
const User = require('../models/User');

class UserRepository {
  constructor() {
    const tableName = process.env.DYNAMODB_TABLE_NAME;
    if (!tableName) {
      throw new Error('DYNAMODB_TABLE_NAME environment variable must be set');
    }

    this.tableName = tableName;
    const dynamoDbClient = new DynamoDBClient({ region: process.env.AWS_REGION || 'us-east-1' });
    this.docClient = DynamoDBDocumentClient.from(dynamoDbClient);
  }

  async create(nombre, email) {
    const id = uuidv4();
    const user = new User(id, nombre, email);

    try {
      await this.docClient.send(
        new PutCommand({
          TableName: this.tableName,
          Item: {
            id: user.id,
            nombre: user.nombre,
            email: user.email,
          },
        })
      );
      return user;
    } catch (error) {
      throw new Error(`Failed to create user in DynamoDB: ${error.message}`);
    }
  }

  async findById(id) {
    try {
      const response = await this.docClient.send(
        new GetCommand({
          TableName: this.tableName,
          Key: { id },
        })
      );

      if (!response.Item) {
        return null;
      }

      return new User(response.Item.id, response.Item.nombre, response.Item.email);
    } catch (error) {
      throw new Error(`Failed to find user in DynamoDB: ${error.message}`);
    }
  }

  async update(id, nombre, email) {
    try {
      // First check if user exists
      const existingUser = await this.findById(id);
      if (!existingUser) {
        return null;
      }

      // Update the user
      await this.docClient.send(
        new PutCommand({
          TableName: this.tableName,
          Item: {
            id,
            nombre,
            email,
          },
        })
      );

      return new User(id, nombre, email);
    } catch (error) {
      throw new Error(`Failed to update user in DynamoDB: ${error.message}`);
    }
  }

  async delete(id) {
    try {
      await this.docClient.send(
        new DeleteCommand({
          TableName: this.tableName,
          Key: { id },
        })
      );
      return true;
    } catch (error) {
      throw new Error(`Failed to delete user in DynamoDB: ${error.message}`);
    }
  }

  async emailExists(email) {
    try {
      const response = await this.docClient.send(
        new QueryCommand({
          TableName: this.tableName,
          IndexName: 'email-index',
          KeyConditionExpression: 'email = :email',
          ExpressionAttributeValues: {
            ':email': email,
          },
        })
      );

      return response.Items && response.Items.length > 0;
    } catch (error) {
      console.error('DynamoDB query error:', error);
      throw new Error(`Failed to check email existence in DynamoDB: ${error.message}`);
    }
  }

  async emailExistsExcept(email, excludeId) {
    try {
      const response = await this.docClient.send(
        new QueryCommand({
          TableName: this.tableName,
          IndexName: 'email-index',
          KeyConditionExpression: 'email = :email',
          ExpressionAttributeValues: {
            ':email': email,
          },
        })
      );

      if (!response.Items || response.Items.length === 0) {
        return false;
      }

      return response.Items.some(item => item.id !== excludeId);
    } catch (error) {
      console.error('DynamoDB query error:', error);
      throw new Error(`Failed to check email existence in DynamoDB: ${error.message}`);
    }
  }
}

// Singleton instance
let instance = null;

function getUserRepository() {
  if (!instance) {
    instance = new UserRepository();
  }
  return instance;
}

module.exports = getUserRepository;
