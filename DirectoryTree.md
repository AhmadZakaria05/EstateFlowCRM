EstateFlowCRM-MAIN/
├── docker-compose.yml
│
├── EstateFlowCRM/
│   ├── .dockerignore
│   ├── .env
│   ├── .env.local
│   ├── .gitattributes
│   ├── .gitignore
│   ├── Dockerfile
│   ├── HELP.md
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── note
│   ├── pom.xml
│   │
│   ├── .mvn/
│   │   └── wrapper/
│   │       └── maven-wrapper.properties
│   │
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/
│       │   │       └── EstateFlowCRM/
│       │   │           ├── EstateFlowCrmApplication.java
│       │   │           ├── commons/
│       │   │           │   ├── ErrorResponse/
│       │   │           │   │   ├── ErrorResponseDto.java
│       │   │           │   │   └── GlobalExceptionHandler.java
│       │   │           │   └── exceptions/
│       │   │           │       └── BusinessException.java
│       │   │           └── platform/
│       │   │               ├── security/
│       │   │               │   ├── config/
│       │   │               │   │   └── SecurityConfig.java
│       │   │               │   ├── controllers/
│       │   │               │   │   └── AuthController.java
│       │   │               │   ├── dto/
│       │   │               │   │   ├── request/
│       │   │               │   │   │   ├── LoginRequest.java
│       │   │               │   │   │   └── RegisterRequest.java
│       │   │               │   │   └── response/
│       │   │               │   │       ├── LoginResponse.java
│       │   │               │   │       └── RegisterResponse.java
│       │   │               │   ├── exceptions/
│       │   │               │   │   ├── InvalidCredentialsException.java
│       │   │               │   │   └── RoleAssignmentNotAllowedException.java
│       │   │               │   ├── mapper/
│       │   │               │   │   ├── AuthMapper.java
│       │   │               │   │   └── AuthMapperImpl.java
│       │   │               │   ├── security/
│       │   │               │   │   ├── ApiSecurityExceptionHandler.java
│       │   │               │   │   ├── CustomUserDetails.java
│       │   │               │   │   ├── JwtAuthenticationFilter.java
│       │   │               │   │   └── UserDetailsServiceImpl.java
│       │   │               │   ├── services/
│       │   │               │   │   ├── AuthService.java
│       │   │               │   │   ├── JwtService.java
│       │   │               │   │   └── Impl/
│       │   │               │   │       └── AuthServiceImpl.java
│       │   │               │   └── util/
│       │   │               │       └── AdminInitializer.java
│       │   │               └── user/
│       │   │                   ├── entities/
│       │   │                   │   ├── Role.java
│       │   │                   │   └── User.java
│       │   │                   ├── exceptions/
│       │   │                   │   └── UserAlreadyExistsException.java
│       │   │                   └── repositories/
│       │   │                       └── UserRepository.java
│       │   └── resources/
│       │       └── application.properties
│       │
│       └── test/
│           └── java/
│               └── com/
│                   └── EstateFlowCRM/
│                       └── platform/
│                           └── security/
│                               ├── controllers/
│                               │   ├── AuthControllerApiTest.java
│                               │   └── AuthSecurityApiTest.java
│                               ├── security/
│                               │   └── ApiSecurityExceptionHandlerTest.java
│                               └── services/
│                                   └── Impl/
│                                       └── AuthServiceImplTest.java
│
└── EstateFlowCRM-Frontend/
├── .dockerignore
├── Dockerfile
├── .gitignore
├── .oxlintrc.json
├── index.html
├── package.json
├── package-lock.json
├── README.md
├── vite.config.js
│
├── public/
│   ├── favicon.svg
│   └── icons.svg
│
└── src/
├── App.jsx
├── index.css
├── main.jsx
├── assets/
│   ├── hero.png
│   └── vite.svg
├── components/
│   └── ProtectedRoute.jsx
├── context/
│   └── AuthContext.jsx
├── pages/
│   ├── Home.css
│   ├── Home.jsx
│   ├── Login.css
│   ├── Login.jsx
│   ├── Register.css
│   └── Register.jsx
└── services/
└── api.js