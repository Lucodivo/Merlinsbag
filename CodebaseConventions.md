# Codebase Conventions

Disclaimer: These are "conventions", not "best practices". Conventions are not always the optimal way
to program or structure code (nor are "best practices", for that matter...). But the aim here is to stick 
with known good practices to produce a more codebase. "Convention"/"Practice" is the goal, not "best". 
As even the worst practice will become easy to reason about if used consistently in a codebase. And a
consistent pattern is easier to refactor when discovering they are a bad practice.

## Compose

### Preview naming convention: Preview- prefix

**Desired Outcome**: Composable function names that scream "I am a preview and not production code".  
Example:

```
@Preview
@Composable
fun PreviewXxxXxx(){
    AppTheme {
        XxxXxx()
    }
}
```

### Custom composable components should not contain default or nullable parameters for the *sole* benefit of composable previews

**Desired Outcome**: Compilation errors for custom composable components with missing or unsuitable parameters.  
**Cons**: Slightly longer to write previews. Slightly longer when refactoring custom composable components.  
**Additional**: Helper preview functions with default values can help reduce the pain of the cons.
Example:

```
@Composable
fun ProductionCameraButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
){
    // ...
}

@Preview
@Composable
fun PreviewProductionButtion(){
    AppTheme {
        ProductionButtion(
            icon = ComposePreviewIcons.Add
            contentDescription = COMPOSE_PREVIEW_CONTENT_DESCRIPTION,
            onClick = {},
        )
    }
}
```