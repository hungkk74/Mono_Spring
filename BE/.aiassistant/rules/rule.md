# CORE OBJECTIVE
Prioritize performance, enterprise-grade architecture, resource optimization, and seamless scalability.

# 1. COMMUNICATION STYLE (TOKEN & TIME OPTIMIZATION)
- **Direct & Concise:** Skip greetings and lengthy explanations. Get straight to the solution.
- **Scannability:** Use bullet points for logic. Keep the layout visual and easy to read.
- **Token Efficiency:** Avoid reprinting entire files. Only provide the modified or newly added code snippets. Use `// ... existing code ...` to hide irrelevant parts.

# 2. ARCHITECTURE & CODE QUALITY (ENTERPRISE STANDARD)
- **Scalability:** Design with loose coupling and high cohesion. Enforce a strict layered architecture.
- **Core Principles:** Strictly adhere to SOLID, DRY (Don't Repeat Yourself), and KISS (Keep It Simple, Stupid) principles.
- **Maintainability:** Use self-documenting names for variables, methods, and classes.

# 3. BACKEND STANDARDS (JAVA / SPRING BOOT)
- **Layered Architecture:** Strictly follow Controller -> Service -> Repository patterns. Keep business logic exclusively in the Service layer. Keep Controllers thin.
- **Data & JPA Optimization:** Prevent N+1 query issues (use `JOIN FETCH`, EntityGraphs, or DTO projections). Optimize HikariCP connection pools and manage `@Transactional` boundaries efficiently.
- **Data Transfer:** Always use DTOs for API requests/responses (use libraries like MapStruct or standard records). Never expose JPA Entities to the web layer.
- **Error Handling:** Implement centralized exception handling using `@RestControllerAdvice` to return standardized and secure API error structures.
- **Security:** Ensure robust Authentication and Authorization using Spring Security (e.g., JWT, OAuth2) at all endpoints.

# 4. FRONTEND STANDARDS (REACT / VITE / NODE.JS)
- **Vite & Node.js Tooling:** Leverage Vite for fast HMR and optimal chunk splitting during the build process. Keep Node.js scripts and package management (npm/yarn/pnpm) clean and optimized.
- **Render Optimization:** Minimize unnecessary re-renders using `useMemo`, `useCallback`, and efficient state management (e.g., Zustand, Redux Toolkit, or React Query for server state caching).
- **Component Architecture:** Break the UI down into independent, highly reusable functional components. Separate business logic (Custom Hooks) from UI (Views).
- **Load Performance:** Apply lazy loading (`React.lazy`) for large routes/modules. Optimize asset sizes and tree-shaking.

# 5. REVIEW & REFACTOR
- **Complexity Check:** Self-evaluate Time & Space Complexity before proposing code.
- **Proactive Alerts:** Flag any potential security vulnerabilities (e.g., XSS, CSRF, SQL Injection), memory leaks, or performance bottlenecks in the existing code immediately.