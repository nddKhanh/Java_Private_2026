---
name: theory-lesson-author
description: Rework or create beginner programming theory lessons that follow a course roadmap, teach concepts before product application, and keep integrated project work in homework.
metadata:
  short-description: Author concept-first lessons
---

# Theory Lesson Author

Use this skill when the user asks to create, reorganize, or revise a programming theory lesson and expects the result to follow their course roadmap.

## Core approach

- Inspect the relevant lesson, homework, and course roadmap before editing. Treat the roadmap’s “core teaching topics” as required coverage and its advanced topics as optional or explicitly advanced.
- Separate the lesson’s purpose from the project’s purpose. The theory lesson may refer to the prior lesson’s problem to explain why a concept matters, and may use small concrete examples. Do not turn its core teaching into building, refactoring, or integrating the course project. Keep project application in homework unless the user explicitly asks otherwise.
- For beginners without prior programming foundations, teach from first principles: explain the term, why it exists, the Java syntax, what each part means, and a small example. Avoid assuming students can infer missing steps from a code listing.
- Keep one coherent conceptual thread across sections where practical. Introduce concepts in a learning sequence: foundational structure first, then related concepts, then advanced material, then one suitable in-class exercise if requested. Respect the user’s requested order over any generic structure.
- Use terminology consistently and explain overloaded vocabulary (e.g. class/object/instance/reference) in plain language.
- Keep examples scoped and runnable in the stated Java version; label examples that replace earlier versions of a class. Do not include project-specific solution code unless requested.

## Exercise specification

When the user wants a small in-class exercise, define one exercise rather than a collection unless they ask for more. Specify the required classes and for each class list its fields and Java types. State that each class needs a constructor and the requested getters/setters without lengthy explanations of those standard methods. For any additional method, give its full signature (visibility where relevant, return type, name, parameter types) and a concise statement of what it does. Include a concrete input, expected output where useful, class relationship/architecture, and required user-visible behavior. Do not invent implementation constraints; include only requested constraints.

If a user provides a concrete exercise idea, preserve its intent and details. Clarify ambiguous behavior only when it materially prevents a useful specification; otherwise make the smallest reasonable assumption and state it.

## Review checklist

Before finishing, verify that:
- The lesson covers the roadmap’s core theory topics and distinguishes optional advanced material.
- Product building is kept in homework; examples only explain concepts or connect to the prior lesson’s problem.
- The sequence is coherent and appropriate for beginners.
- The exercise can be implemented from its class, field, method, input, output, and behavior specifications without guessing.
- Any requested concept questions are conceptual and self-contained rather than dependent on unprovided code context.
- The final document retains the user’s desired language, tone, and scope.
