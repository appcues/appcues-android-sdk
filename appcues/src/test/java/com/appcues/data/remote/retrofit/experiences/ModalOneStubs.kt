package com.appcues.data.remote.retrofit.experiences

import com.appcues.data.remote.response.ProfileResponse
import com.appcues.data.remote.response.TacoResponse
import com.appcues.data.remote.response.experience.ExperienceResponse
import com.appcues.data.remote.response.experience.ExperienceThemeResponse
import com.appcues.data.remote.response.step.StepActionResponse
import com.appcues.data.remote.response.step.StepContentResponse
import com.appcues.data.remote.response.step.StepItemContentResponse
import com.appcues.data.remote.response.step.StepItemResponse
import com.appcues.data.remote.response.step.StepResponse
import com.appcues.data.remote.response.styling.SizeResponse
import com.appcues.data.remote.response.styling.StyleColorResponse
import com.appcues.data.remote.response.styling.StyleGradientColorResponse
import com.appcues.data.remote.response.styling.StyleResponse
import com.appcues.data.remote.response.styling.StyleShadowResponse
import com.appcues.data.remote.response.trait.TraitConfigResponse
import com.appcues.data.remote.response.trait.TraitResponse

internal val modalOneStub = TacoResponse(
    checklists = arrayListOf(),
    contents = arrayListOf(),
    experiences = arrayListOf(
        ExperienceResponse(
            id = "9f4baa80-8f6a-41b1-a7b9-979da5c175e2",
            name = "POC Modal One",
            tags = arrayListOf(),
            theme = ExperienceThemeResponse(),
            actions = hashMapOf(),
            traits = arrayListOf(),
            steps = arrayListOf(
                StepResponse(
                    id = "68c0d4b4-4909-4d4a-9ce4-7af8b04efab2",
                    contentType = "application/json",
                    content = StepContentResponse(
                        id = "2cf7dbf7-c6be-4130-b642-85861f9c6b6a",
                        type = "stack",
                        orientation = "vertical",
                        style = StyleResponse(
                            horizontalAlignment = "center",
                            marginBottom = 25
                        ),
                        items = arrayListOf(
                            StepItemResponse(
                                id = "8e46637d-071a-4405-a9dd-dec4a64e98b8",
                                type = "stack",
                                orientation = "horizontal",
                                distribution = "equal",
                                items = arrayListOf(
                                    StepItemResponse(
                                        id = "80bb63b2-2db2-44a4-b5fc-61c3d7f5c889",
                                        type = "block",
                                        blockType = "image",
                                        content = StepItemContentResponse(
                                            type = "image",
                                            id = "f2affaaa-0883-42f8-a313-73a28cc2d0b4",
                                            imageUrl = "https://res.cloudinary.com/dnjrorsut/image/upload/v1635971825/98227/oh5drlvojb1spaetc1ol.jpg",
                                            contentMode = "fill",
                                            intrinsicSize = SizeResponse(
                                                width = 1920,
                                                height = 1280,
                                            ),
                                            style = StyleResponse(
                                                backgroundColor = StyleColorResponse(
                                                    light = "#236"
                                                )
                                            )
                                        )
                                    )
                                )
                            ),
                            StepItemResponse(
                                id = "678a3466-8f26-4530-9391-82c03bc2cebf",
                                type = "stack",
                                orientation = "horizontal",
                                distribution = "equal",
                                items = arrayListOf(
                                    StepItemResponse(
                                        id = "e905d8bf-b1ad-4401-adfe-2f7e01878fa1",
                                        type = "block",
                                        blockType = "text",
                                        content = StepItemContentResponse(
                                            id = "d52cd085-3f57-4218-8eb2-973f654a5acf",
                                            type = "text",
                                            text = "Ready to make your\nworkflow simpler?",
                                            style = StyleResponse(
                                                marginTop = 20,
                                                marginBottom = 5,
                                                fontName = "Lato-Black",
                                                fontSize = 20,
                                                textAlignment = "center",
                                                lineSpacing = 4,
                                                foregroundColor = StyleColorResponse(
                                                    light = "#394455",
                                                    dark = "#fff"
                                                )
                                            )
                                        )
                                    )
                                )
                            ),
                            StepItemResponse(
                                id = "dffddbe8-0ec8-4045-8681-25c60f7f9ccf",
                                type = "stack",
                                orientation = "horizontal",
                                distribution = "equal",
                                items = arrayListOf(
                                    StepItemResponse(
                                        id = "a25bc0a0-f527-4ec6-bc8f-3f8d8ff4d0e9",
                                        type = "block",
                                        blockType = "text",
                                        content = StepItemContentResponse(
                                            id = "d52cd085-3f57-4218-8eb2-973f654a5dcf",
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
                                                lineSpacing = 2,
                                                foregroundColor = StyleColorResponse(
                                                    light = "#394455",
                                                    dark = "#fff"
                                                )
                                            )
                                        )
                                    )
                                )
                            ),
                            StepItemResponse(
                                id = "a4ac4eb8-f833-4be1-8b14-d58562f11aa8",
                                type = "stack",
                                orientation = "horizontal",
                                distribution = "equal",
                                items = arrayListOf(
                                    StepItemResponse(
                                        id = "f4c4f89e-4c8a-4c9d-9a8a-80c8bbfa8fa7",
                                        type = "block",
                                        blockType = "button",
                                        content = StepItemContentResponse(
                                            id = "c34f7d02-443d-497c-ac3a-1a9b42af9dd8",
                                            type = "button",
                                            content = StepItemContentResponse(
                                                id = "a839e508-3dd9-47ac-8d5a-4cd122c3177d",
                                                type = "text",
                                                text = "Button 1",
                                                style = StyleResponse(
                                                    fontName = "Lato-Bold",
                                                    fontSize = 17,
                                                    foregroundColor = StyleColorResponse(
                                                        light = "#fff"
                                                    )
                                                )
                                            ),
                                            style = StyleResponse(
                                                paddingTop = 8,
                                                paddingLeading = 18,
                                                paddingBottom = 8,
                                                paddingTrailing = 18,
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
                    traits = arrayListOf(
                        TraitResponse(
                            type = "@appcues/modal",
                            config = TraitConfigResponse(
                                presentationStyle = "dialog",
                                skippable = true,
                                backdropColor = StyleColorResponse(
                                    light = "#0000004d",
                                    dark = "#ffffff4d",
                                ),
                                style = StyleResponse(
                                    cornerRadius = 8,
                                    backgroundColor = StyleColorResponse(
                                        light = "#fff",
                                        dark = "#000"
                                    ),
                                    shadow = StyleShadowResponse(
                                        color = StyleColorResponse(light = "#777777ee"),
                                        radius = 3,
                                        x = 0,
                                        y = 2
                                    )
                                )
                            )
                        )
                    ),
                    actions = hashMapOf(
                        "c34f7d02-443d-497c-ac3a-1a9b42af9dd8" to arrayListOf(
                            StepActionResponse(
                                on = "tap",
                                type = "@appcues/close"
                            )
                        )
                    )
                )
            )
        )
    ),
    performedQualifications = true,
    profile = ProfileResponse(abGroup = 1),
    qualificationReason = "forced",
    requestId = "814EF333-6C88-4354-985B-9B5FE930F8DB"
)
