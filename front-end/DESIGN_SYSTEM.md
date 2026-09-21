# Design System DataPulse

## Fonte de verdade

MUI fornece tema, tokens semânticos, componentes acessíveis e estados de interação. Tailwind CSS v4 fornece somente layout, responsividade e utilitários de composição. Tokens visuais não devem ser duplicados em `className`, `sx` ou CSS local.

Tema e tokens vivem em `src/app/theme.ts` e `src/app/designTokens.ts`. O sistema usa modo claro, contraste WCAG AA, azul como ação primária e verde como confirmação/resultado positivo.

## Paleta

- Primária: `#2563EB`; hover/escura: `#1D4ED8`.
- Secundária: `#047857`; hover/escura: `#065F46`.
- Fundo: `#F8FAFC`; superfície: `#FFFFFF`; superfície suave: `#F1F5F9`.
- Texto principal: `#0F172A`; texto secundário: `#475569`; borda: `#DCE6F0`.
- Estados: sucesso `#15803D`, aviso `#B45309`, erro `#B91C1C`, informação `#0369A1`.

Gráficos usam escala categórica fixa em `designTokens.chart`; não usar gradientes nem cores aleatórias.

## Componentes

Componentes compartilhados ficam em `src/components/ui`: `SectionCard`, `MetricCard`, `StatusBadge`, `StatePanel`, `PaginationActions` e `DataTableShell`. Páginas compõem fluxo; features mantêm regras de domínio. Criar abstração somente quando houver reuso real.

`StatusBadge` é usado para estados de ingestão. Toda consulta precisa representar loading, vazio, erro recuperável e estado de sucesso. Mensagens globais usam `Snackbar` + `Alert` do MUI.

O estado `PROCESSING` usa `LoaderCircle` com animação `motion-safe:animate-spin`; a animação não deve impedir leitura do status nem desrespeitar preferência de movimento reduzido.

## Ícones e acessibilidade

Usar `lucide-react` com imports nomeados. Ícones decorativos recebem `aria-hidden`; ícones interativos sempre têm label acessível e área mínima de 44px. Foco visível, navegação por teclado, `aria-live` para progresso e contraste AA são obrigatórios.

## MUI + Tailwind

`StyledEngineProvider enableCssLayer`, `cssVariables: true` e camadas `theme, base, mui, components, utilities` preservam precedência previsível. MUI controla componentes; Tailwind v4 controla layout (`grid`, `flex`, espaçamento responsivo e largura). Não aplicar reset, cores ou tipografia concorrentes.

## Validação visual

Validar desktop, largura móvel de 320px, foco por teclado, estados de loading/erro/vazio e overflow horizontal de tabelas. Alterações devem passar `npm run lint` e `npm run build`.
