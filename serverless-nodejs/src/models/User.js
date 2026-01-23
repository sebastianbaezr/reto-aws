class User {
  constructor(id, nombre, email) {
    this.id = id;
    this.nombre = nombre;
    this.email = email;
  }

  static fromObject(obj) {
    return new User(obj.id, obj.nombre, obj.email);
  }

  toJSON() {
    return {
      id: this.id,
      nombre: this.nombre,
      email: this.email
    };
  }
}

module.exports = User;
