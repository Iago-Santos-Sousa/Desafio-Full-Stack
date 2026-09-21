# Regras locais do front-end

- Stack: React 19, Vite 8, TypeScript estrito, React Router, TanStack Query, MUI 7, Tailwind CSS v4 e Lucide React.
- Leia `../AGENTS.md`, `../TASK.md`, `../PLAN.md` e `DESIGN_SYSTEM.md` antes de alterar UI.
- MUI é fonte de verdade para tema, tokens, componentes e acessibilidade. Tailwind v4 é reservado a layout/responsividade; não duplicar cores, tipografia ou reset.
- Use tokens de `src/app/designTokens.ts` e tema de `src/app/theme.ts`. Evite cores hexadecimais em features.
- Use componentes compartilhados em `src/components/ui` antes de criar novo padrão.
- Use imports nomeados de `lucide-react`; ícones decorativos têm `aria-hidden`, ícones interativos têm label e área mínima de 44px.
- Mantenha estados inicial, loading, vazio, erro e sucesso. Use `Snackbar` + `Alert` para feedback global.
- TanStack Query somente em hooks de `src/hooks/api`; componentes não chamam `useQuery` ou `useMutation` diretamente.
- Não introduza `any`; prefira interfaces, unions discriminadas e `ReactNode`.
- Preserve contratos REST, paginação por cursor, polling e comportamento de filtros de datas.
- Use `@/` para imports internos, com alias sincronizado em `vite.config.ts` e `tsconfig.app.json`; imports de bibliotecas externas permanecem com seus escopos originais.
- Prettier 3.8.3 usa `.prettierrc.json` e `.prettierignore`; `npm run format:check` e opcional e `npm run format` altera fontes somente quando solicitado.
- ESLint usa `eslint-config-prettier/flat` por ultimo na configuracao; nao adicionar `eslint-plugin-prettier` sem necessidade.
- Mantenha `strict: true` nos projetos TypeScript app e node. Execute `npm run typecheck` antes de declarar alteração pronta.
- Consulte documentação oficial via Context7 quando alterar MUI, Tailwind, Router, Query, Chart.js ou Lucide.
- Valide com `npm run lint` e `npm run build`; revise acessibilidade e responsividade manualmente.
