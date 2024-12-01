# Sala

Sala (Stack-based Assembly-like Language) is a very simple language inspired by CIL and Java bytecode

## Examples

```
; hello world

push "Hello, world!"
print
```

```
; simple loop to test jumping that prints 0-9

push 0

:loop
dup
print
push "\n"
print

push 1
add

dup
push 10
jlt loop
```

## Roadmap

- [x] Basics of a turing complete language
- [ ] Variables
- [ ] Instructions written the language itself
- [ ] User created instructions