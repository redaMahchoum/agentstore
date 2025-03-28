# AgentStore Backend API Documentation

## Authentication System

The application uses Keycloak for authentication and authorization. Frontend applications must implement OAuth 2.0 and OpenID Connect to authenticate users and obtain tokens for API access.

### Keycloak Configuration

- **Server URL**: `http://localhost:8081`
- **Realm**: `agentstore`
- **Client ID**: `agentstore-app`
- **Public Client**: `false` (confidential client requiring secret)
- **Client Secret**: `secret` (configured in Keycloak)

### Authentication Flows

#### 1. Authorization Code Flow (recommended for web applications)

This is the standard OpenID Connect flow for web applications.

**Authorization Endpoint:**
```
http://localhost:8081/realms/agentstore/protocol/openid-connect/auth
```

**Token Endpoint:**
```
http://localhost:8081/realms/agentstore/protocol/openid-connect/token
```

**Logout Endpoint:**
```
http://localhost:8081/realms/agentstore/protocol/openid-connect/logout
```

Example configuration for a frontend application using the Authorization Code flow:
```javascript
// Auth configuration example (using auth libraries like auth0/auth-js)
const authConfig = {
  authority: 'http://localhost:8081/realms/agentstore',
  clientId: 'agentstore-app',
  clientSecret: 'secret', // For backend code only - never expose in frontend
  redirectUri: 'http://localhost:3000/callback',
  responseType: 'code',
  scope: 'openid profile email'
};
```

#### 2. Resource Owner Password Credentials (for testing/development)

This flow allows direct username/password authentication for clients that can securely store credentials.

**Token Endpoint Request:**
```
POST http://localhost:8081/realms/agentstore/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&
client_id=agentstore-app&
client_secret=secret&
username={username}&
password={password}
```

#### 3. Refresh Token Flow

When an access token expires, use the refresh token to obtain a new access token without user re-authentication.

**Token Endpoint Request:**
```
POST http://localhost:8081/realms/agentstore/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=refresh_token&
client_id=agentstore-app&
client_secret=secret&
refresh_token={refresh_token}
```

### JWT Token Information

- Access tokens are JWT tokens with a default expiration of 5 minutes
- Refresh tokens have a default expiration of 30 minutes
- The JWT contains user details and roles in standard claims format
- The backend expects tokens in the `Authorization` header using the Bearer scheme

Example JWT structure (decoded):
```json
{
  "exp": 1682956799,
  "iat": 1682956199,
  "auth_time": 1682956199,
  "jti": "...",
  "iss": "http://localhost:8081/realms/agentstore",
  "sub": "user-id",
  "typ": "Bearer",
  "azp": "agentstore-app",
  "session_state": "...",
  "acr": "1",
  "realm_access": {
    "roles": ["user", "admin"]
  },
  "resource_access": {
    "agentstore-app": {
      "roles": ["user", "admin"]
    }
  },
  "scope": "openid profile email",
  "sid": "...",
  "email_verified": true,
  "name": "John Doe",
  "preferred_username": "john.doe",
  "given_name": "John",
  "family_name": "Doe",
  "email": "john.doe@example.com"
}
```

## API Endpoints

All API endpoints are prefixed with `/api`. Authentication is required for most endpoints except those explicitly marked as public.

### Authentication Endpoints

#### 1. User Registration

```
POST /api/auth/register
```

**Request Body:**
```json
{
  "username": "john.doe",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "SecureP@ss123",
  "enabled": true,
  "emailVerified": false,
  "roles": ["user"]
}
```

**Response:** User DTO with 201 Created status

**Note:** This endpoint is accessible without authentication as it's part of the public registration flow.

#### 2. Token Debug (for development)

```
GET /api/auth/token-debug
```

**Response:** JWT token details including claims and authorities (roles)

**Requires Authentication:** Yes

### User Management Endpoints

#### 1. Get All Users

```
GET /api/users
```

**Response:** List of UserDTO objects
**Requires Authentication:** Yes
**Required Role:** admin

#### 2. Get User by ID

```
GET /api/users/{id}
```

**Path Parameter:** User UUID
**Response:** UserDTO
**Requires Authentication:** Yes
**Required Role:** admin or user's own profile

#### 3. Get User by Username

```
GET /api/users/username/{username}
```

**Path Parameter:** Username
**Response:** UserDTO
**Requires Authentication:** Yes
**Required Role:** admin or user's own profile

#### 4. Create User (Admin)

```
POST /api/users
```

**Request Body:** UserDTO
**Response:** Created UserDTO
**Requires Authentication:** Yes
**Required Role:** admin

#### 5. Update User

```
PUT /api/users/{id}
```

**Path Parameter:** User UUID
**Request Body:** UserDTO
**Response:** Updated UserDTO
**Requires Authentication:** Yes
**Required Role:** admin or user's own profile

#### 6. Delete User

```
DELETE /api/users/{id}
```

**Path Parameter:** User UUID
**Response:** 204 No Content
**Requires Authentication:** Yes
**Required Role:** admin

#### 7. Assign Role to User

```
POST /api/users/{id}/roles/{role}
```

**Path Parameters:**
- User UUID
- Role name

**Response:** 204 No Content
**Requires Authentication:** Yes
**Required Role:** admin

#### 8. Remove Role from User

```
DELETE /api/users/{id}/roles/{role}
```

**Path Parameters:**
- User UUID
- Role name

**Response:** 204 No Content
**Requires Authentication:** Yes
**Required Role:** admin

## Data Transfer Objects (DTOs)

### UserDTO

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "keycloakId": "f8392-3289-...",
  "username": "john.doe",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "SecureP@ss123", // Only used for creation/update
  "enabled": true,
  "emailVerified": false,
  "roles": ["user", "admin"]
}
```

## Frontend Application Setup

### Required Dependencies

1. **Keycloak JS Adapter** - For direct integration with Keycloak
   ```
   npm install keycloak-js@22.0.0
   ```

2. **Alternative OAuth/OIDC Libraries**
   - Auth0 SPA SDK: `npm install @auth0/auth0-react`
   - OIDC Client: `npm install oidc-client-ts`

### Environment Configuration

Create a `.env` file in your frontend project with these variables:

```
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_KEYCLOAK_URL=http://localhost:8081
REACT_APP_KEYCLOAK_REALM=agentstore
REACT_APP_KEYCLOAK_CLIENT_ID=agentstore-app
```

### Keycloak Integration Example (React)

```javascript
// keycloak.js
import Keycloak from 'keycloak-js';

const keycloakConfig = {
  url: process.env.REACT_APP_KEYCLOAK_URL,
  realm: process.env.REACT_APP_KEYCLOAK_REALM,
  clientId: process.env.REACT_APP_KEYCLOAK_CLIENT_ID
};

const keycloak = new Keycloak(keycloakConfig);

export default keycloak;
```

```javascript
// App.js
import { useState, useEffect } from 'react';
import keycloak from './keycloak';

function App() {
  const [authenticated, setAuthenticated] = useState(false);
  const [user, setUser] = useState(null);

  useEffect(() => {
    keycloak.init({
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
      pkceMethod: 'S256' // Use PKCE for public clients
    }).then((authenticated) => {
      setAuthenticated(authenticated);
      if (authenticated) {
        setUser({
          id: keycloak.subject,
          username: keycloak.tokenParsed.preferred_username,
          name: keycloak.tokenParsed.name,
          email: keycloak.tokenParsed.email,
          roles: keycloak.tokenParsed.realm_access.roles
        });
      }
    });
  }, []);

  const login = () => {
    keycloak.login();
  };

  const logout = () => {
    keycloak.logout({ redirectUri: window.location.origin });
  };

  return (
    <div>
      {!authenticated && (
        <button onClick={login}>Login</button>
      )}
      {authenticated && (
        <div>
          <p>Welcome, {user?.name}!</p>
          <button onClick={logout}>Logout</button>
        </div>
      )}
    </div>
  );
}

export default App;
```

### Making Authenticated API Requests

```javascript
// api.js
import keycloak from './keycloak';

const API_URL = process.env.REACT_APP_API_URL;

async function fetchWithAuth(url, options = {}) {
  // Ensure the token is still valid
  await keycloak.updateToken(30); // Refresh if less than 30 seconds remaining
  
  // Add the Authorization header
  const headers = {
    ...options.headers,
    Authorization: `Bearer ${keycloak.token}`
  };
  
  return fetch(`${API_URL}${url}`, {
    ...options,
    headers
  }).then(response => {
    if (!response.ok) {
      // Handle error responses
      if (response.status === 401) {
        // Token might be invalid
        keycloak.logout();
      }
      throw new Error('API request failed');
    }
    return response.json();
  });
}

export const userApi = {
  getCurrentUser: () => {
    const username = keycloak.tokenParsed.preferred_username;
    return fetchWithAuth(`/users/username/${username}`);
  },
  
  getAllUsers: () => {
    return fetchWithAuth('/users');
  },
  
  registerUser: (userData) => {
    return fetch(`${API_URL}/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(userData)
    }).then(response => response.json());
  }
};
```

## Security Considerations

1. **NEVER store access tokens in local storage** - They are vulnerable to XSS attacks
2. **Use HttpOnly cookies or memory storage** for token management
3. **Implement PKCE** (Proof Key for Code Exchange) for public clients
4. **Set up proper CORS configuration** on both the frontend and backend
5. **Validate tokens** on every request in the backend
6. **Use HTTPS** in production environments
7. **Implement proper token refresh** mechanisms
8. **Handle token expiration** gracefully in the UI
9. **Log out users properly** by invalidating the session on both client and server

## Versioning Information

- **Backend Version**: 1.0.0
- **API Version**: v1
- **Keycloak Version**: 22.0.0
- **Spring Boot Version**: 3.1.x
- **Java Version**: 17

## Local Development Setup

1. **Start the Keycloak server**:
   ```
   docker-compose -f docker-compose-keycloak.yml up -d
   ```

2. **Start the backend application**:
   ```
   docker-compose -f docker-compose-app.yml up -d
   ```
   
   OR
   
   ```
   ./mvnw spring-boot:run
   ```

3. **Access the Keycloak admin console**:
   ```
   http://localhost:8081/admin
   Username: admin
   Password: admin
   ```

4. **Swagger API Documentation**:
   ```
   http://localhost:8080/api/swagger-ui.html
   ```

## Troubleshooting

1. **Invalid Token Errors**:
   - Check clock synchronization between services
   - Verify Keycloak is running and accessible
   - Ensure correct audience is configured

2. **CORS Issues**:
   - Ensure the frontend origin is whitelisted in Keycloak
   - Check network tab for specific CORS errors

3. **Role/Permission Problems**:
   - Verify roles are correctly mapped in Keycloak
   - Check JWT token payload for correct role assignments

For additional support, contact the backend development team. 