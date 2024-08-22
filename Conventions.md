# Conventions

Disclaimer: These are "conventions", not "best practices". Conventions are not always the optimal way
to program or structure code (nor are "best practices", for that matter...). But the aim here is to stick
with known *good* practices to produce a more cohesive codebase. "Convention"/"Practice" is the goal. *Not* "best".
As even the worst practice will become easy to reason about if used consistently. And a consistent pattern is
easier to refactor when discovering they are indeed a bad practice.

## Unit Testing

### Test Structure

Tests are structured using the Arrange-Act-Assert pattern.
1) *Arrange* the initial state, data, and measurements for the test.
2) Perform the *Actions* that are under test.
3) Measure and *Assert* the initial and expected results.

**Desired Outcome**: Both assertions and setup can build up and harm the readable purpose of the test. Although a great test function
name can go a long way, isolating the actions-under-test clearly demonstrates the intention of said test.
**Note**: This convention perfectly demonstrates the difference between "convention" and "best practice". Why not assert initial state
during the *Arrange* portion? Why take measurements in the *Assert* portion? For the first question, maybe it seems counterintuitive to
assert outside of the *Assert* portion? For the second question, maybe it's fair to say that the purity of the *Action* portion is the main
appeal of the convention. However, the best answer to both of these questions is that it has simply been chosen to be convention.

Ex:
```
@Test
fun purgeDatabase() = runBlocking{
  val entityCount = 10
  articleDao.insertArticles(*createArticleEntity(entityCount))
  val allArticlesBefore = articleDao.getArticlesCount().first()

  purgeDao.purgeDatabase()

  val allArticlesAfter = articleDao.getArticlesCount().first()
  assertEquals(0, allArticlesAfter)
  assertEquals(entityCount, allArticlesBefore)
}
```

## Compose
### Preview naming convention: Preview- prefix

**Desired Outcome**: Function names that scream "I am a compose preview" for improved code completion and searchability.
```
@Preview
@Composable
fun PreviewXxxXxx(){
    AppTheme {
        XxxXxx()
    }
}
```

### Components should *not* accept default or nullable parameters for the *sole* benefit of composable previews
**Desired Outcome**: Compilation errors for custom composable components with missing parameters.
**Cons**: Slightly longer to write previews. Slightly longer when refactoring custom composable components.  
**Additional**: Helper preview functions with default values can help reduce the pain of the cons.

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