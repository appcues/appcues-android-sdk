package com.appcues.data.mapper.step

import com.appcues.data.remote.response.step.StepContentResponse
import com.appcues.data.remote.response.styling.SizeResponse
import com.appcues.data.remote.response.styling.StyleColorResponse
import com.appcues.data.remote.response.styling.StyleGradientColorResponse
import com.appcues.data.remote.response.styling.StyleResponse
import com.appcues.domain.entity.ExperienceComponent.ButtonComponent
import com.appcues.domain.entity.ExperienceComponent.HorizontalStackComponent
import com.appcues.domain.entity.ExperienceComponent.ImageComponent
import com.appcues.domain.entity.ExperienceComponent.TextComponent
import com.appcues.domain.entity.ExperienceComponent.VerticalStackComponent
import com.appcues.domain.entity.styling.ComponentColor
import com.appcues.domain.entity.styling.ComponentDistribution
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.UUID

class StepContentMapperTest {

    private val mapper = StepContentMapper()

    @Test
    fun `map SHOULD transform StepContentResponse into VerticalStackComponent WHEN type is stack AND orientation vertical`() {
        // GIVEN
        val randomId = UUID.randomUUID()
        val textRandomId = UUID.randomUUID()
        val from = StepContentResponse(
            id = randomId,
            type = "stack",
            orientation = "vertical",
            items = arrayListOf(
                StepContentResponse(
                    id = UUID.randomUUID(),
                    type = "block",
                    content = StepContentResponse(
                        id = textRandomId,
                        type = "text",
                        text = "Sample Text",
                        style = StyleResponse(
                            fontSize = 20,
                            foregroundColor = StyleColorResponse(light = "#000")
                        )
                    )
                )
            )
        )
        // WHEN
        val result = mapper.map(from)
        // THEN
        assertThat(result).isInstanceOf(VerticalStackComponent::class.java)
        with(result as VerticalStackComponent) {
            assertThat(id).isEqualTo(randomId)
            assertThat(items).hasSize(1)
            assertThat(items[0]).isInstanceOf(TextComponent::class.java)
            with(items[0] as TextComponent) {
                assertThat(id).isEqualTo(textRandomId)
                assertThat(text).isEqualTo("Sample Text")
                assertThat(style.fontSize).isEqualTo(20)
                assertThat(style.foregroundColor?.light).isEqualTo(0xFF000000)
                assertThat(style.foregroundColor?.dark).isEqualTo(0xFF000000)
            }
        }
    }

    @Test
    fun `map SHOULD transform StepContentResponse into HorizontalStackComponent WHEN type is stack AND orientation horizontal`() {
        // GIVEN
        val randomId = UUID.randomUUID()
        val textRandomId = UUID.randomUUID()
        val from = StepContentResponse(
            id = randomId,
            type = "stack",
            orientation = "horizontal",
            distribution = "equal",
            items = arrayListOf(
                StepContentResponse(
                    id = UUID.randomUUID(),
                    type = "block",
                    content = StepContentResponse(
                        id = textRandomId,
                        type = "text",
                        text = "Sample Text",
                        style = StyleResponse(
                            fontSize = 20,
                            foregroundColor = StyleColorResponse(light = "#000")
                        )
                    )
                )
            )
        )
        // WHEN
        val result = mapper.map(from)
        // THEN
        assertThat(result).isInstanceOf(HorizontalStackComponent::class.java)
        with(result as HorizontalStackComponent) {
            assertThat(id).isEqualTo(randomId)
            assertThat(items).hasSize(1)
            assertThat(distribution).isEqualTo(ComponentDistribution.EQUAL)
            assertThat(items[0]).isInstanceOf(TextComponent::class.java)
            with(items[0] as TextComponent) {
                assertThat(id).isEqualTo(textRandomId)
                assertThat(text).isEqualTo("Sample Text")
                assertThat(style.fontSize).isEqualTo(20)
                assertThat(style.foregroundColor?.light).isEqualTo(0xFF000000)
                assertThat(style.foregroundColor?.dark).isEqualTo(0xFF000000)
            }
        }
    }

    @Test
    fun `map SHOULD transform StepContentResponse into TextComponent WHEN type is not stack AND content type is text`() {
        // GIVEN
        val randomId = UUID.randomUUID()
        val from = StepContentResponse(
            id = UUID.randomUUID(),
            type = "block",
            content = StepContentResponse(
                id = randomId,
                type = "text",
                text = "Sample Text",
                style = StyleResponse(
                    fontSize = 20,
                    foregroundColor = StyleColorResponse(light = "#000")
                )
            )
        )
        // WHEN
        val result = mapper.map(from)
        // THEN
        assertThat(result).isInstanceOf(TextComponent::class.java)
        with(result as TextComponent) {
            assertThat(id).isEqualTo(randomId)
            assertThat(text).isEqualTo("Sample Text")
            assertThat(style.fontSize).isEqualTo(20)
            assertThat(style.foregroundColor?.light).isEqualTo(0xFF000000)
            assertThat(style.foregroundColor?.dark).isEqualTo(0xFF000000)
        }
    }

    @Test
    fun `map SHOULD transform StepContentResponse into ButtonComponent WHEN type is not stack and content type is button`() {
        // GIVEN
        val randomId = UUID.randomUUID()
        val textRandomId = UUID.randomUUID()
        val from = StepContentResponse(
            id = UUID.randomUUID(),
            type = "block",
            content = StepContentResponse(
                id = randomId,
                type = "button",
                content = StepContentResponse(
                    id = textRandomId,
                    type = "text",
                    text = "Button 1",
                    style = StyleResponse(
                        fontSize = 17,
                        foregroundColor = StyleColorResponse(light = "#fff", dark = "#000")
                    )
                ),
                style = StyleResponse(
                    backgroundGradient = StyleGradientColorResponse(
                        colors = arrayListOf(
                            StyleColorResponse(light = "#5C5CFF"),
                            StyleColorResponse(light = "#8960FF")
                        ),
                        startPoint = "leading",
                        endPoint = "trailing",
                    ),
                    cornerRadius = 6,
                )
            )
        )
        // WHEN
        val result = mapper.map(from)
        // THEN
        assertThat(result).isInstanceOf(ButtonComponent::class.java)
        with(result as ButtonComponent) {
            assertThat(id).isEqualTo(randomId)
            assertThat(style.backgroundGradient).hasSize(2)
            assertThat(style.backgroundGradient!![0]).isEqualTo(ComponentColor(light = 0xFF5C5CFF, dark = 0xFF5C5CFF))
            assertThat(style.backgroundGradient[1]).isEqualTo(ComponentColor(light = 0xFF8960FF, dark = 0xFF8960FF))
            assertThat(content).isInstanceOf(TextComponent::class.java)
            with(content as TextComponent) {
                assertThat(id).isEqualTo(textRandomId)
                assertThat(text).isEqualTo("Button 1")
                assertThat(style.fontSize).isEqualTo(17)
                assertThat(style.foregroundColor?.light).isEqualTo(0xFFFFFFFF)
                assertThat(style.foregroundColor?.dark).isEqualTo(0xFF000000)
            }
        }
    }

    @Test
    fun `map SHOULD transform StepContentResponse into ImageComponent WHEN type is block and content type is image`() {
        // GIVEN
        val randomId = UUID.randomUUID()
        val imageUrl = "https://appcues.test.image/image.jpg"
        val from = StepContentResponse(
            id = UUID.randomUUID(),
            type = "block",
            content = StepContentResponse(
                id = randomId,
                type = "image",
                imageUrl = imageUrl,
                contentMode = "fill",
                accessibilityLabel = "Image Label",
                intrinsicSize = SizeResponse(
                    width = 1920,
                    height = 1280,
                ),
                style = StyleResponse(
                    backgroundColor = StyleColorResponse(light = "#000")
                )
            )
        )
        // WHEN
        val result = mapper.map(from)
        // THEN
        assertThat(result).isInstanceOf(ImageComponent::class.java)
        with(result as ImageComponent) {
            assertThat(id).isEqualTo(randomId)
            assertThat(url).isEqualTo(imageUrl)
            assertThat(accessibilityLabel).isEqualTo("Image Label")
            assertThat(intrinsicSize?.width).isEqualTo(1920)
            assertThat(intrinsicSize?.height).isEqualTo(1280)
            assertThat(style.backgroundColor).isNotNull()
            assertThat(style.backgroundColor?.light).isEqualTo(0xFF000000)
        }
    }
}
