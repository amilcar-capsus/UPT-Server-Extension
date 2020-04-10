# Suitability formula

```math
\begin{aligned}
study\_area &=\{ geometry_{user\_layer\_data} \text\textbar user\_layer\_id_{user\_layer\_data} = study\_area\_id \}

\\

mmu\_layers &= \{
    \\
    & user\_layer\_id_{user\_layer\_data}
    \\
    &, geometry_{user\_layer\_data}
    \\
    &, value =
    \begin{cases}
        0   &\text{if } value_{{property\_json}_{user\_layer\_data}} = \emptyset
        \\
        value_{{property\_json}_{user\_layer\_data}} &\text{others}
    \end{cases}
    \\
    &, mmu\_code_{{property\_json}_{user\_layer\_data}}
    \\
    \}

\\
\end{aligned}
```

```math
\begin{aligned}
filter\_pol &= \{ geometry_{user\_layer\_data} \text\textbar  user\_layer\_id_{user\_layer\_data} = user\_layer\_id _{st\_filters} \land  id _{st\_filters} \in filters\_list\}

\\

intersected &=
\begin{cases}
    filter\_pol_i &\text{if } intersected == \emptyset
    \\
    intersected \bigcap filter\_pol_i &\text{if } opetation = "intersection"
    \\
    intersected \bigcup filter\_pol_i &\text{if } opetation\ != "intersection"
\end{cases}

\\

\end{aligned}
```

```math
\begin{aligned}

filters &= \begin{cases}
        study\_area &\text{if } filters\_list == \emptyset
        \\
        intersected &\text{if } others
    \end{cases}
\Biggr\rbrace

\\
\end{aligned}
```

```math
\begin{aligned}

study\_filtered("geometry","study\_area") &=
    \begin{cases}
        study\_area,study\_area &\text{if } filters\_list == \emptyset
        \\
        study\_area \bigcap intersected,study\_area &\text{if } operation != "difference"
        \\
        study\_area \setminus intersected, study\_area &\text{if } operation = "difference"
    \end{cases}

\\
\end{aligned}
```

```math
\begin{aligned}

config &=\bigg\{
    user\_layer\_id_{settings\_list}
    ,st\_layer\_id_{settings\_list}
    ,normalization\_method_{settings\_list}
    ,smaller\_better_{settings\_list}
    ,range\_max_{settings\_list}
    ,weight_{settings\_list}
    \bigg\}

\\
\end{aligned}
```

```math
\begin{aligned}

user\_config &=\bigg\{
    \\ &user\_layer\_id_{st\_layers}
    \\ &,st\_layer\_id_{{conf}_{config}}
    \\ &,normalization\_method_{{conf}_{config}}
    \\ &,smaller\_better_{{conf}_{config}}
    \\ &,range\_max_{{conf}_{config}}
    \\ &,weight_{{conf}_{config}}
    \\
    \Bigg\vert
    &id_{st\_layers} = user\_layer\_id_{config}
    \bigg\}

\\
\end{aligned}
```

```math
\begin{aligned}

vals\_obs\_max\_min &= \bigg\{
    \\ &user\_layer\_id_{user\_layer\_data}
    \\ &,\max(layer\_field_{st\_layers_{property\_json}})
    \\ &,\min(layer\_field_{st\_layers_{property\_json}})
    \\ &,stddev(layer\_field_{st\_layers_{property\_json}})
    \\ &,avg(layer\_field_{st\_layers_{property\_json}})
    \Bigg\vert
    \\& user\_layer\_id_{st\_layers} = user\_layer\_id_{user\_layer\_data}
    \\& \land id_{st\_layers} \in layers\_list
    \\& \land geometry_{user\_layer\_data} = study\_area_{study\_filtered}
    \\& \{ user\_layer\_id_{user\_layer\_data \} }
\bigg\}

\\
\end{aligned}
```

```math
\begin{aligned}

vals\_settings= \{
    \\ & user\_layer\_id_{user\_config}
    \\ &, st\_layers\_id_{user\_config}
    \\ &, smaller\_better_{user\_config}
    \\ &, weight_{user\_config}
    \\ &, normalization\_method_{user\_config}
    \\ &, stddev
    \\ &, avg
    \\ &, range\_max=\begin{cases}
    range\_max_{user\_config} &\text{if } normalization\_method_{user\_config}!=1 \\
    max_{vals\_obs\_max\_min} &\text{if } normalization\_method_{user\_config}=1
    \end{cases}
    \\ &, range\_min=\begin{cases}
    range\_min_{user\_config} &\text{if } normalization\_method_{user\_config}!=1 \\
    min_{vals\_obs\_max\_min} &\text{if } normalization\_method_{user\_config}=1
    \end{cases}
    \\ \Bigg\vert
    & id_{st\_settings} = st\_layers\_id_{user\_config}
    \land user\_layer\_id_{user\_config} = user\_layer\_id_{vals\_obs\_max\_min}
    \land st\_layers\_id_{user\_config} \in  layers\_list
\}

\\
\end{aligned}
```

```math
\begin{aligned}

total &=\{
    (sum(weight_{vals\_settings}),count(weight_{vals\_settings})) \vert st\_layers\_id_{vals\_settings} \in layers\_list
\}

\\
\end{aligned}
```

```math
\begin{aligned}

mmu\_settings &= \{
    \\& user\_layer\_id_{user\_layer\_data}
    \\& ,geometry_{user\_layer\_data}
    \\& ,geometry_{study\_filtered}
    \\& ,smaller\_better_{vals\_settings}
    \\& ,weight=weight_{vals\_settings}/weight_{total}
    \\& ,range\_max_{vals\_settings}
    \\& ,range\_min_{vals\_settings}
    \\& ,normalization\_method_{vals\_settings}
    \\& ,stddev_{vals\_settings}
    \\& ,mean_{vals\_settings}
    \\& ,value_{property\_json_{user\_layer\_data}}
    \\& ,mmu\_code_{property\_json_{user\_layer\_data}}
    \\& \bigg\vert
    user\_layer\_id_{st\_layers} = user\_layer\_id_{user\_layer\_data}
    \land user\_layer\_id_{user\_layer\_data} = user\_layer\_id_{vals\_settings}
    \\& \land st\_layers\_id_{vals\_settings} \in layers\_list
    \\& \land geometry_{study\_filtered} \cap geometry_{user\_layer\_data}
\}

\\
\end{aligned}
```

```math
\begin{aligned}

mmu\_index &= \bigg\{
    \\& user\_layer\_id_{mmu\_settings}
    \\& ,mmu\_code
    \\& ,smaller\_better
    \\& ,weight
    \\& ,value=\begin{cases}
    \begin{cases}
        0 &\text{if } weight*(value - range\_min) / (range\_max-range\_min) < 0
        \\
        1 &\text{if } weight*(value - range\_min) / (range\_max-range\_min) > 1
        \\
        weight*(value - range\_min) / (range\_max-range\_min) &\text{others }
    \end{cases} &\text{if } normalization\_method_{mmu\_settings} !=3
    \\
    \\
    \begin{cases}
        0 &\text{if } weight*(value - mean) / dev < 0
        \\
        1 &\text{if } weight*(value - mean) / dev > 1
        \\
        weight*(value - mean) / dev &\text{others }
    \end{cases} &\text{others }
    \end{cases}
    \\& \bigg\vert mmu\_settings
\\\bigg\}

\end{aligned}
```

```math
\begin{aligned}

mmu\_adjust=\{
    \\& user\_layer\_id
    \\& ,mmu\_code
    \\& ,value=\begin{cases}
        value &\text{if } smaller\_better = 0
        \\
        1-value &\text{others }
    \end{cases}
    \\ &\bigg\vert mmu\_index
\}

\end{aligned}
```

```math
\begin{aligned}

mmu\_index\_resutls &= \{
    \\& mmu\_code_{mmu\_adjust},
    \\&  \sum value_{mmu\_adjust}/num\_layers_{total}
    \\& \vert \{ mmu\_code_{mmu\_adjust}\}
    \\
\}

\end{aligned}
```

```math
\begin{aligned}

mmu\_geometries = \{
    \\&
    value_{mmu\_index},
    \\&
    (\bigcup geometry_{mmu\_layers}) \cap
    geometry_{study\_filtered}
    \\&
    \vert mmu\_code_{mmu\_layers} = mmu\_code_{mmu\_index\_resutls},
    \{ mmu\_code_{mmu\_adjust} \}
    \\
\}

\end{aligned}
```
