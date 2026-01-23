const User = require('../models/User');

class UserRepository {
  constructor() {
    this.users = new Map();
    this.nextId = 4;
    this.initializeHardcodedUsers();
  }

  initializeHardcodedUsers() {
    // Hardcoded initial users
    this.users.set(1, new User(1, 'Juan Pérez', 'juan.perez@bancolombia.com'));
    this.users.set(2, new User(2, 'María López', 'maria.lopez@bancolombia.com'));
    this.users.set(3, new User(3, 'Carlos Rodríguez', 'carlos.rodriguez@bancolombia.com'));
  }

  create(nombre, email) {
    const id = this.nextId++;
    const user = new User(id, nombre, email);
    this.users.set(id, user);
    return user;
  }

  findById(id) {
    return this.users.get(id) || null;
  }

  update(id, nombre, email) {
    if (!this.users.has(id)) {
      return null;
    }
    const user = new User(id, nombre, email);
    this.users.set(id, user);
    return user;
  }

  delete(id) {
    return this.users.delete(id);
  }

  emailExists(email) {
    for (const user of this.users.values()) {
      if (user.email.toLowerCase() === email.toLowerCase()) {
        return true;
      }
    }
    return false;
  }

  emailExistsExcept(email, id) {
    for (const [userId, user] of this.users.entries()) {
      if (userId !== id && user.email.toLowerCase() === email.toLowerCase()) {
        return true;
      }
    }
    return false;
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
