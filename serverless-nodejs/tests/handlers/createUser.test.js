const { createUser } = require('../../src/handlers/createUser');

describe('createUser handler', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  test('should create a new user successfully', async () => {
    const event = {
      body: JSON.stringify({
        nombre: 'New User',
        email: 'newuser@test.com'
      })
    };

    const response = await createUser(event, {});

    expect(response.statusCode).toBe(201);
    const body = JSON.parse(response.body);
    expect(body.id).toBeDefined();
    expect(body.nombre).toBe('New User');
    expect(body.email).toBe('newuser@test.com');
  });

  test('should return 400 if email already exists', async () => {
    const event = {
      body: JSON.stringify({
        nombre: 'Juan Pérez',
        email: 'juan.perez@bancolombia.com'
      })
    };

    const response = await createUser(event, {});

    expect(response.statusCode).toBe(400);
    const body = JSON.parse(response.body);
    expect(body.error).toContain('Email already exists');
  });

  test('should return 400 if nombre is missing', async () => {
    const event = {
      body: JSON.stringify({
        email: 'test@test.com'
      })
    };

    const response = await createUser(event, {});

    expect(response.statusCode).toBe(400);
  });

  test('should return 400 if email is missing', async () => {
    const event = {
      body: JSON.stringify({
        nombre: 'Test User'
      })
    };

    const response = await createUser(event, {});

    expect(response.statusCode).toBe(400);
  });

  test('should return 400 if email format is invalid', async () => {
    const event = {
      body: JSON.stringify({
        nombre: 'Test User',
        email: 'invalidemail'
      })
    };

    const response = await createUser(event, {});

    expect(response.statusCode).toBe(400);
  });

  test('should return 400 if body is empty', async () => {
    const event = {
      body: ''
    };

    const response = await createUser(event, {});

    expect(response.statusCode).toBe(400);
  });

  test('should return 400 if body is null', async () => {
    const event = {
      body: null
    };

    const response = await createUser(event, {});

    expect(response.statusCode).toBe(400);
  });

  test('should return 400 if JSON is invalid', async () => {
    const event = {
      body: '{invalid json}'
    };

    const response = await createUser(event, {});

    expect(response.statusCode).toBe(400);
  });
});
