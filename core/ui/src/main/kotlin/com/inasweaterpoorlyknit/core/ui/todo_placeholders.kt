package com.inasweaterpoorlyknit.core.ui

// Using these variables allows me to easily track things like accessibility content descriptions
// by just searching for usages. Doing accessibility right will require research. Should the description
// describe the functionality of a button or only the content? What about in the likely situation that the image
// content is unknowable to the developer or application?
const val TODO_ICON_CONTENT_DESCRIPTION = "TODO: icon content description"
const val TODO_IMAGE_CONTENT_DESCRIPTION = "TODO: image content description"


// if the image or icon is decorative and offers no additional information needed for screen reader users
// or if the image or icon is repetitive and a content description is better provided by it's container
const val REDUNDANT_CONTENT_DESCRIPTION = ""

// Don't currently know what to do with these but they should be documented if a solution is determined
const val ARTICLE_IMAGE_CONTENT_DESCRIPTION = ""
