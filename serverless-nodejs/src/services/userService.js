const getUserRepository = require('./userRepository');
const validateEmail = require('../utils/validator');
const { BadRequestError, NotFoundError } = require('../utils/errorHandler');

class UserService {
  constructor() {
    this.repository = getUserRepository();
  }

  async createUser(nombre, email) {
    // Validate input
    if (!nombre || typeof nombre !== 'string' || nombre.trim() === '') {
      throw new BadRequestError('Nombre is required');
    }

    if (!email || typeof email !== 'string' || email.trim() === '') {
      throw new BadRequestError('Email is required');
    }

    if (!validateEmail(email)) {
      throw new BadRequestError('Invalid email format');
    }

    if (await this.repository.emailExists(email)) {
      throw new BadRequestError(`Email already exists: ${email}`);
    }

    return await this.repository.create(nombre, email);
  }

  async getUser(userId) {
    const user = await this.repository.findById(userId);
    if (!user) {
      throw new NotFoundError(`User not found with ID: ${userId}`);
    }
    return user;
  }

  async updateUser(userId, nombre, email) {
    // Verify user exists
    const existingUser = await this.repository.findById(userId);
    if (!existingUser) {
      throw new NotFoundError(`User not found with ID: ${userId}`);
    }

    // Validate input
    if (!nombre || typeof nombre !== 'string' || nombre.trim() === '') {
      throw new BadRequestError('Nombre is required');
    }

    if (!email || typeof email !== 'string' || email.trim() === '') {
      throw new BadRequestError('Email is required');
    }

    if (!validateEmail(email)) {
      throw new BadRequestError('Invalid email format');
    }

    if (await this.repository.emailExistsExcept(email, userId)) {
      throw new BadRequestError(`Email already exists: ${email}`);
    }

    return await this.repository.update(userId, nombre, email);
  }

  async deleteUser(userId) {
    const user = await this.repository.findById(userId);
    if (!user) {
      throw new NotFoundError(`User not found with ID: ${userId}`);
    }

    await this.repository.delete(userId);
    return { message: 'User deleted successfully' };
  }
}

module.exports = UserService;
