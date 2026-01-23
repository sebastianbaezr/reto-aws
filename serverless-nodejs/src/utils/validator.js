const EMAIL_PATTERN = /^[A-Za-z0-9+_.-]+@(.+)$/;

function validateEmail(email) {
  if (!email || typeof email !== 'string') {
    return false;
  }
  return EMAIL_PATTERN.test(email);
}

function validateUser(user) {
  const errors = [];

  if (!user) {
    return ['User cannot be null'];
  }

  if (!user.nombre || typeof user.nombre !== 'string' || user.nombre.trim() === '') {
    errors.push('Nombre is required');
  }

  if (!user.email || typeof user.email !== 'string' || user.email.trim() === '') {
    errors.push('Email is required');
  } else if (!validateEmail(user.email)) {
    errors.push('Invalid email format');
  }

  return errors;
}

module.exports = validateEmail;
module.exports.validateEmail = validateEmail;
module.exports.validateUser = validateUser;
