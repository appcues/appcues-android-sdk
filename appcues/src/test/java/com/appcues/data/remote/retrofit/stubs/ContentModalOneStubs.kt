@file:Suppress("MaxLineLength")

package com.appcues.data.remote.retrofit.stubs

import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.experience.ExperienceResponse
import com.appcues.data.remote.response.step.StepContainerResponse
import com.appcues.data.remote.response.step.StepContentResponse
import com.appcues.data.remote.response.step.StepResponse
import com.appcues.data.remote.response.styling.StyleColorResponse
import com.appcues.data.remote.response.styling.StyleGradientColorResponse
import com.appcues.data.remote.response.styling.StyleResponse
import com.appcues.data.remote.response.styling.StyleShadowResponse
import com.appcues.data.remote.response.styling.StyleSizeResponse
import com.appcues.data.remote.response.trait.TraitResponse
import java.util.UUID

internal val contentModalOneStubs = ExperienceResponse(
    id = UUID.fromString("9f4baa80-8f6a-41b1-a7b9-979da5c175e2"),
    state = "PUBLISHED",
    type = "mobile",
    publishedAt = 1643221968554,
    name = "POC Modal One",
    theme = null,
    actions = null,
    nextContentId = null,
    redirectUrl = null,
    traits = arrayListOf(),
    steps = arrayListOf(
        StepContainerResponse(
            id = UUID.fromString("6c2b7488-309c-432f-b62e-9f8539b46c9d"),
            children = arrayListOf(
                StepResponse(
                    id = UUID.fromString("68c0d4b4-4909-4d4a-9ce4-7af8b04efab2"),
                    type = "modal",
                    content = StepContentResponse(
                        id = UUID.fromString("2cf7dbf7-c6be-4130-b642-85861f9c6b6a"),
                        type = "stack",
                        orientation = "vertical",
                        style = StyleResponse(),
                        items = arrayListOf(
                            StepContentResponse(
                                id = UUID.fromString("8e46637d-071a-4405-a9dd-dec4a64e98b8"),
                                type = "stack",
                                orientation = "horizontal",
                                distribution = "equal",
                                items = arrayListOf(
                                    StepContentResponse(
                                        id = UUID.fromString("80bb63b2-2db2-44a4-b5fc-61c3d7f5c889"),
                                        type = "block",
                                        content = StepContentResponse(
                                            id = UUID.fromString("f2affaaa-0883-42f8-a313-73a28cc2d0b4"),
                                            type = "image",
                                            imageUrl = "https://res.cloudinary.com/dnjrorsut/image/upload/v1635971825/98227/oh5drlvojb1spaetc1ol.jpg",
                                            accessibilityLabel = "Mountains at night",
                                            contentMode = "fill",
                                            blurHash = "LDAmob}[k6tSxyoMNFR*005RaiV?",
                                            intrinsicSize = StyleSizeResponse(
                                                width = 1920,
                                                height = 1280,
                                            ),
                                            style = StyleResponse(
                                                backgroundColor = StyleColorResponse(
                                                    light = "#223366"
                                                )
                                            )
                                        )
                                    )
                                )
                            ),
                            StepContentResponse(
                                id = UUID.fromString("678a3466-8f26-4530-9391-82c03bc2cebf"),
                                type = "stack",
                                orientation = "horizontal",
                                distribution = "equal",
                                items = arrayListOf(
                                    StepContentResponse(
                                        id = UUID.fromString("e905d8bf-b1ad-4401-adfe-2f7e01878fa1"),
                                        type = "block",
                                        content = StepContentResponse(
                                            id = UUID.fromString("d52cd085-3f57-4218-8eb2-973f654a5acf"),
                                            type = "text",
                                            text = "Ready to make your\nworkflow simpler?",
                                            style = StyleResponse(
                                                marginTop = 20,
                                                marginBottom = 5,
                                                fontName = "Lato-Black",
                                                fontSize = 20,
                                                textAlignment = "center",
                                                lineHeight = 24,
                                                foregroundColor = StyleColorResponse(
                                                    light = "#394455",
                                                    dark = "#ffffff"
                                                )
                                            )
                                        )
                                    )
                                )
                            ),
                            StepContentResponse(
                                id = UUID.fromString("dffddbe8-0ec8-4045-8681-25c60f7f9ccf"),
                                type = "stack",
                                orientation = "horizontal",
                                distribution = "equal",
                                items = arrayListOf(
                                    StepContentResponse(
                                        id = UUID.fromString("a25bc0a0-f527-4ec6-bc8f-3f8d8ff4d0e9"),
                                        type = "block",
                                        content = StepContentResponse(
                                            id = UUID.fromString("d52cd085-3f57-4218-8eb2-973f654a5dcf"),
                                            type = "text",
                                            text = "Take a few moments to learn how to best use our features.",
                                            style = StyleResponse(
                                                marginTop = 10,
                                                marginLeading = 30,
                                                marginBottom = 15,
                                                marginTrailing = 30,
                                                fontName = "Lato-Regular",
                                                fontSize = 17,
                                                textAlignment = "center",
                                                lineHeight = 19,
                                                foregroundColor = StyleColorResponse(
                                                    light = "#394455",
                                                    dark = "#ffffff"
                                                )
                                            )
                                        )
                                    )
                                )
                            ),
                            StepContentResponse(
                                id = UUID.fromString("a4ac4eb8-f833-4be1-8b14-d58562f11aa8"),
                                type = "stack",
                                orientation = "horizontal",
                                distribution = "equal",
                                style = StyleResponse(),
                                items = arrayListOf(
                                    StepContentResponse(
                                        id = UUID.fromString("f4c4f89e-4c8a-4c9d-9a8a-80c8bbfa8fa7"),
                                        type = "block",
                                        content = StepContentResponse(
                                            id = UUID.fromString("c34f7d02-443d-497c-ac3a-1a9b42af9dd8"),
                                            type = "button",
                                            content = StepContentResponse(
                                                id = UUID.fromString("a839e508-3dd9-47ac-8d5a-4cd122c3177d"),
                                                type = "text",
                                                text = "Button 1",
                                                style = StyleResponse(
                                                    fontName = "Lato-Bold",
                                                    fontSize = 17,
                                                    foregroundColor = StyleColorResponse(
                                                        light = "#ffffff"
                                                    )
                                                )
                                            ),
                                            style = StyleResponse(
                                                paddingTop = 8,
                                                paddingLeading = 18,
                                                paddingBottom = 8,
                                                paddingTrailing = 18,
                                                marginBottom = 25,
                                                backgroundGradient = StyleGradientColorResponse(
                                                    colors = arrayListOf(
                                                        StyleColorResponse(light = "#5C5CFF"),
                                                        StyleColorResponse(light = "#8960FF")
                                                    ),
                                                    startPoint = "leading",
                                                    endPoint = "trailing",
                                                ),
                                                cornerRadius = 6,
                                                shadow = StyleShadowResponse(
                                                    color = StyleColorResponse(light = "#777777ee"),
                                                    radius = 3,
                                                    x = 0,
                                                    y = 2
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    traits = arrayListOf(),
                    actions = hashMapOf(
                        UUID.fromString("c34f7d02-443d-497c-ac3a-1a9b42af9dd8") to arrayListOf(
                            ActionResponse(
                                on = "tap",
                                type = "@appcues/close",
                                config = null,
                            )
                        )
                    )
                )
            ),
            actions = hashMapOf(),
            traits = arrayListOf(
                TraitResponse(
                    type = "@appcues/modal",
                    config = hashMapOf(
                        "presentationStyle" to "dialog",
                        "style" to hashMapOf(
                            "cornerRadius" to 8.toDouble(),
                            "backgroundColor" to hashMapOf(
                                "light" to "#ffffff",
                                "dark" to "#000000"
                            ),
                            "shadow" to hashMapOf(
                                "color" to hashMapOf(
                                    "light" to "#777777ee"
                                ),
                                "radius" to 3.0,
                                "x" to 0.0,
                                "y" to 2.0
                            )
                        )
                    )
                ),
                TraitResponse(
                    type = "@appcues/skippable",
                ),
                TraitResponse(
                    type = "@appcues/backdrop",
                    config = hashMapOf(
                        "backgroundColor" to hashMapOf(
                            "light" to "#0000004d",
                            "dark" to "#ffffff4d"
                        )
                    )
                )
            ),
        )
    )
)
