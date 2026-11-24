

The resen of this architecture is:
Low Coupling between features, Maintainability, Separation of Concerns and Team Collaboration.

Low Coupling تقليل الاعتمادية 
Separation of Concerns فصل المسؤوليات


com.blurized.estateflow
├── common
│   └── exceptions
│       └── BusinessException.java
│
│
├── clients/
├── pipeline/
├── activities/
├── followups/
├── feedback/
├── properties/
├── reports/
│
└── platform
    ├
    ├── user
    │   ├── entities
    │   ├── repositories
    │   └── exceptions
    │       ├── UserAlreadyExistsException.java
    │       └── InvalidRegistrationRoleException.java
    │
    └── security/
        │
        ├── config/
        │   ├── SecurityConfig.java
        │   ├── PasswordConfig.java
        │   └── JwtConfig.java
        │
        ├── controller/
        │   └── AuthController.java
        │
        ├── service/
        │   ├── AuthService.java
        │   ├── UserService.java
        │   └── JwtService.java
        │
        │
        │
        ├── dto/
        │   ├── request/
        │   │   ├── LoginRequest.java
        │   │   ├── CreateUserRequest.java
        │   │   └── RefreshTokenRequest.java
        │   │
        │   └── response/
        │       ├── AuthResponse.java
        │       └── UserResponse.java
        │
        ├── security/
        │   └── CustomUserDetails.java
        │
        └── exception/
            └── AuthException.java


