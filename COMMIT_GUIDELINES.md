# 🧾 Commit Message Style Guide

This guide helps keep commit messages consistent, clear, and meaningful across the project.

---

## 🔧 Commit Message Format

```
<type>: <short summary>
<optional empty line>
<optional bullet list with details>
```

**Example:**

```
feat: 🌟 add search functionality to user list

- Enables fuzzy search
- Adds debounce for performance
```

---

## ✳️ Commit Types

| Type        | Description                                 | Emoji (optional) |
|-------------|---------------------------------------------|------------------|
| `feat`      | 🌟 New feature                              | 🌟               |
| `fix`       | 🐛 Bug fix                                  | 🐛               |
| `docs`      | 📝 Documentation changes (README, comments) | 📝               |
| `style`     | 💄 Code style (no logic changes)            | 💄               |
| `refactor`  | ♻️ Code restructuring                       | ♻️               |
| `test`      | ✅ Adding or changing tests                  | ✅                |
| `chore`     | 🔧 Maintenance tasks (deps, config, etc.)   | 🔧               |
| `perf`      | ⚡ Performance improvements                  | ⚡                |
| `ci`        | 👷 CI/CD related changes                    | 👷               |

---

## 📌 Rules

- Limit the first line to **72 characters**
- Use **present tense** (e.g., `add` not `added`)
- **Don't end the first line with a period**
- **Use emojis** to quickly communicate context (optional but encouraged)
- Leave an empty line between the summary and details (if using bullet list)

---

## ✅ Examples

```
feat: 🌟 add login feature with validation
fix: 🐛 fix null pointer on user creation
docs: 📝 update API section in README
refactor: ♻️ simplify permission checking
style: 💄 reformat code with consistent indentation
test: ✅ add unit tests for UserService
chore: 🔧 update Gradle dependencies
```

---

## 💡 Tips

- Keep your commits **focused**: one purpose per commit
- Use bullet points if listing multiple changes
- Consider using tools like [Commitizen](https://github.com/commitizen/cz-cli) for consistent formatting

---