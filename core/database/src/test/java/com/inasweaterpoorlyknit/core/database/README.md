Database tests should really only be focused on testing complex queries or a very low number of dead simple tests to verify the integrity of the database or a table.
If it feels that we are testing Room's implementation and not a complex query, the test should probably be deleted to allow greater freedom in future refactors.