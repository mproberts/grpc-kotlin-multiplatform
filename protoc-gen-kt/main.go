package main

import (
	pgs "github.com/lyft/protoc-gen-star"
)

func main() {
	pgs.Init(
		pgs.DebugEnv("DEBUG"),
	).RegisterModule(
		RenderTemplate(encoderTpl, ".encode.kt"),
		RenderTemplate(decoderTpl, ".decode.kt"),
		RenderTemplate(dataClassTpl, ".data.kt"),
		RenderTemplate(builderTpl, ".builder.kt"),
		RenderTemplate(serviceTpl, ".service.kt"),
	).Render()
}

const serviceTpl = `package {{ package . }}

import gg.roll.common.proto.tools.*
import gg.roll.common.proto.tools.LinkedByteArray
import gg.roll.common.proto.tools.MutableLinkedByteArray
import gg.roll.common.proto.tools.ProtobufOutputStream
import gg.roll.common.proto.tools.ScopedProtobufReader
import gg.roll.common.proto.tools.GrpcService
import gg.roll.common.net.RpcClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlin.js.JsName
{{ range imports . }}
import {{ . }}{{ end }}
{{- define "service" }}
interface {{ name . }}Service {

    companion object {
        val Descriptor = GrpcService.ServiceDescriptor(
            "{{ package . }}", "{{ name . }}", listOf(
                {{ range .Methods }}
                GrpcService.MethodDescriptor<{{ name .Input }}, {{ name .Output }}>(
                    "{{ originalName . }}",
                    {{ name .Input }}.Companion::readFrom,
                    {{ name .Output }}::writeTo,
                    {{ if .ClientStreaming }}true{{ else }}false{{ end }},
                    {{ if .ServerStreaming }}true{{ else }}false{{ end }}
                ),
                {{- end }}
            )
        )
    }

    {{ range .Methods }}
    {{ if .ServerStreaming }}{{ else }}suspend {{ end }}fun {{ name . }}(request: {{ if .ClientStreaming }}Flow<{{ name .Input }}>{{ else }}{{ name .Input }}{{- end }}): {{ if .ServerStreaming }}Flow<{{ name .Output }}>{{ else }}{{ name .Output }}{{- end }}
    {{- end }}
}

class {{ name . }}ServiceRpc(rpc: RpcClient): {{ name . }}Service, GrpcService(rpc, "{{ package . }}", "{{ name . }}") {
    {{ range .Methods }}
    override {{ if .ServerStreaming }}{{ else }}suspend {{ end }}fun {{ name . }}(request: {{ if .ClientStreaming }}Flow<{{ name .Input }}>{{ else }}{{ name .Input }}{{- end }}): {{ if .ServerStreaming }}Flow<{{ name .Output }}>{{ else }}{{ name .Output }}{{- end }} = client{{ if .ClientStreaming }}Stream{{ else }}Unary{{- end }}Server{{ if .ServerStreaming }}Stream{{ else }}Unary{{- end }}("{{ .Name }}", request, {{ name .Input }}::writeTo, {{ name .Output }}.Companion::readFrom)
    {{- end }}
}
{{ end }}
{{ range .Services }}{{ template "service" . }}{{ end -}}
`
const decoderTpl = `package {{ package . }}
{{ range imports . }}
import {{ . }}{{ end }}
{{ range packageImports . }}
import {{ . }}.readFrom{{ end }}
import gg.roll.common.proto.tools.ProtobufReader
import gg.roll.common.proto.tools.ProtobufInputStream
import kotlin.js.JsName
{{- define "message" }}
inline fun {{ fullyQualifiedName . }}.Companion.fromByteArray(bytes: ByteArray): {{ fullyQualifiedName . }} = ProtobufInputStream()
    .let { stream ->
        stream.addBytes(bytes)
        stream.read {
            {{ fullyQualifiedName . }}.readFrom(it)
        }
    }

fun {{ fullyQualifiedName . }}.Companion.readFrom(reader: ProtobufReader): {{ fullyQualifiedName . }} = {{ builderName . }}().apply {
    while (reader.nextField()) {
        when (reader.currentFieldNumber) {
        {{ range .OneOfs }}
            {{- range .Fields }}
            {{- if .Type.IsEmbed }}
            {{ .Descriptor.Number }} -> {
                {{ .OneOf.Descriptor.Name }}.{{ name . }} = reader.readField { fieldReader ->
                    {{ name .Type.Embed }}.readFrom(fieldReader)
                }
            }
            {{ else if .Type.IsEnum }}
            {{ else if isBytes . }}
            {{ .Descriptor.Number }} -> {
                {{ .OneOf.Descriptor.Name }}.{{ name . }} = reader.readBytes()
            }
            {{- else }}
            {{ .Descriptor.Number }} -> {
                {{ .OneOf.Descriptor.Name }}.{{ name . }} = reader.{{ readerMethod .Type.ProtoType }}()
            }
            {{- end }}
            {{ end }}
        {{ end }}
        {{- range .NonOneOfFields }}
            {{ if .Type.IsEmbed }}{{ .Descriptor.Number }} -> {
                {{ name . }} = reader.readField { fieldReader ->
                    {{ type . }}.readFrom(fieldReader)
                }
            }
            {{ else if .Type.IsMap }}
            {{ else if .Type.IsRepeated }}{{ .Descriptor.Number }} -> { {{- if .Type.Element.IsEmbed }}{{ name . }} = {{ name . }} + reader.readField { fieldReader ->
                    {{ name .Type.Element.Embed }}.readFrom(fieldReader)
                }{{- else }}
				reader.readField { fieldReader ->
                    while (fieldReader.isByteAvailable()) {
                        {{ name . }} = {{ name . }} + fieldReader.{{ readerMethod .Type.ProtoType }}()
                    }
                }
				{{ end }}
            }
            {{ else if .Type.IsEnum }}{{ .Descriptor.Number }} -> {
                val enumValue = reader.readInt32()

                {{ name . }} = {{ type . }}.values().first { it.value == enumValue }
            }
            {{ else if isBytes . }}{{ .Descriptor.Number }} -> { {{ name . }} = reader.readBytes() }
            {{ else }}{{ .Descriptor.Number }} -> { {{ name . }} = reader.{{ readerMethod .Type.ProtoType }}() }
            {{ end }}
        {{- end }}
            else -> {}
        }
    }
}.build()
{{ end }}
{{ range .AllMessages }}{{ template "message" . }}{{ end -}}
`

const encoderTpl = `package {{ package . }}
{{ range imports . }}
import {{ . }}{{ end }}
{{ range packageImports . }}
import {{ . }}.writeTo{{ end }}
import gg.roll.common.proto.tools.ProtobufWriter
import gg.roll.common.proto.tools.ProtobufOutputStream
import kotlin.js.JsName
{{- define "message" }}
@JsName("{{ escapedFullyQualifiedName . }}ToByteArray")
inline fun {{ fullyQualifiedName . }}.toByteArray() = ProtobufOutputStream()
    .apply {
        write {
            this@toByteArray.writeTo(it)
        }
    }
    .toByteArray()

@JsName("{{ escapedFullyQualifiedName . }}WriteTo")
fun {{ fullyQualifiedName . }}.writeTo(writer: ProtobufWriter) {
    {{ range .OneOfs }}
    when ({{ .Descriptor.Name }}) {
        {{- range .Fields }}
        {{- if .Type.IsEmbed }}
        is {{ name .Message }}.OneOf{{ upperCamel .OneOf.Descriptor.Name }}.{{ name . }} -> writer.encode({{ .Descriptor.Number }}) { ({{ .OneOf.Descriptor.Name }} as {{ name .Message }}.OneOf{{ upperCamel .OneOf.Descriptor.Name }}.{{ name . }}).value.writeTo(this) }
        {{- else }}
        is {{ name .Message }}.OneOf{{ upperCamel .OneOf.Descriptor.Name }}.{{ name . }} -> writer.encode({{ .Descriptor.Number }}, ({{ .OneOf.Descriptor.Name }} as {{ name .Message }}.OneOf{{ upperCamel .OneOf.Descriptor.Name }}.{{ name . }}).value)
        {{- end }}
        {{ end }}
        else -> {}
    }
    {{ end }}
    {{- range $index, $_ := .NonOneOfFields }}
    {{ if .Type.IsEmbed }}
    {{ name . }}?.let { value ->
        writer.encode({{ .Descriptor.Number }}) {
            value.writeTo(this)
        }
    }
    {{ else if .Type.IsMap }}
    {{ else if .Type.IsRepeated }}
	{{ if .Type.Element.IsEmbed }}
    {{ name . }}.forEach { value ->
        writer.encode({{ .Descriptor.Number }}) {
            value.writeTo(this)
        }
    }
	{{ else }}
    writer.encode({{ .Descriptor.Number }}) {
        {{ name . }}.forEach { value ->
            this.encodeRaw(value)
        }
    }
    {{ end }}
    {{ else if .Type.IsEnum }}
    if ({{ name . }} != {{ default . }}) writer.encode({{ .Descriptor.Number }}, {{ name . }}.ordinal){{ else if isBytes . }}
    if ({{ name . }}.isNotEmpty()) writer.encode({{ .Descriptor.Number }}, {{ name . }}){{ else }}
    if ({{ name . }} != {{ default . }}) writer.encode({{ .Descriptor.Number }}, {{ name . }}){{ end }}
    {{ end }}

    unknownFields?.let { writer.write(it) }
}
{{ end -}}
{{ range .AllMessages }}{{ template "message" . }}{{ end -}}`

const dataClassTpl = `package {{ package . }}
{{ range imports . }}
import {{ . }}{{ end }}
{{- define "enum" }}
enum class {{ simpleName . }}(val value: Int) {
    {{- range $index, $_ := .Values }}{{ if $index }},{{ end }}
    {{ name . }}({{ .Descriptor.Number }}){{ end }};
}
{{ end -}}

{{- define "message" }}
data class {{ simpleName . }}(
    {{- range $index, $_ := .OneOfs }}
    val {{ .Descriptor.Name }}: OneOf{{ upperCamel .Descriptor.Name }}? = null,{{ end }}
    {{- range $index, $_ := .NonOneOfFields }}
    val {{ name . }}: {{ type . }} = {{ default . }},{{ end }}
    val unknownFields: ByteArray? = null
) {
    {{- range .Messages }}{{ include "message" . | indent 4 }}{{ end }}
    {{- range .Enums }}{{ include "enum" . | indent 4 }}{{ end }}

    {{- range $index, $_ := .OneOfs }}
    sealed class OneOf{{ upperCamel .Descriptor.Name }}(protected open val value: Any) {
        {{- range .Fields }}
        data class {{ name . }}(public override val value: {{ typeNonNull . }}) : OneOf{{ upperCamel .OneOf.Descriptor.Name }}(value){{ end }}

        fun <V> getOrNull(): V? {
            return this.value as? V
        }
    }
    {{ end }}
    companion object {}

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as {{ simpleName .}}

        {{- range $index, $_ := .OneOfs }}
        if ({{ .Descriptor.Name }} != other.{{ .Descriptor.Name }}) return false {{ end }}
        {{- range $index, $_ := .NonOneOfFields }}

        {{ if isBytes . -}}
        if (!{{ name . }}.contentEquals(other.{{ name . }})) return false
        {{ else -}}
        if ({{ name . }} != other.{{ name . }}) return false
        {{ end }}{{ end }}

        if (unknownFields != null) {
            if (other.unknownFields == null) return false
            if (!unknownFields.contentEquals(other.unknownFields)) return false
        } else if (other.unknownFields != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 0

        {{- range $index, $_ := .OneOfs }}
        result = 31 * result + ({{ .Descriptor.Name }}?.hashCode() ?: 0){{ end }}
        {{- range $index, $_ := .NonOneOfFields }}
        {{ if isBytes . -}}
        result = 31 * result + {{ name . }}.contentHashCode()
        {{ else if .Type.IsEmbed -}}
        result = 31 * result + ({{ name . }}?.hashCode() ?: 0)
        {{ else -}}
        result = 31 * result + ({{ name . }}.hashCode() ?: 0)
        {{ end }}{{ end }}

        result = 31 * result + (unknownFields?.contentHashCode() ?: 0)
        return result
    }
}
{{ end }}
{{ range .Messages }}{{ template "message" . }}{{ end -}}
{{ range .Enums }}{{ template "enum" .}}{{ end -}}`

const builderTpl = `package {{ package . }}
{{ range imports . }}
import {{ . }}{{ end }}
{{ range packageImports . }}
import {{ . }}.*{{ end }}
{{ range builderImports . }}
import {{ . }}{{ end }}
{{- define "message" }}

inline fun {{ fullyQualifiedCompanionName . }}(builder: {{ builderName . }}.() -> Unit): {{ name . }} {
    return {{ builderName . }}().apply(builder).build()
}

inline fun {{ fullyQualifiedName . }}.copy(builder: {{ builderName . }}.() -> Unit): {{ name . }} {
    return {{ builderName . }}(this).apply(builder).build()
}

class {{ simpleBuilderName . }} {
    constructor()

    constructor(copy: {{ name . }}) {
        {{- range $index, $_ := .NonOneOfFields }}
        builderCopy.{{ name . }} = copy.{{ name . }}{{ end }}
        {{- range $index, $_ := .OneOfs }}
        builderCopy.{{ name . }} = copy.{{ name . }}{{ end }}
        builderCopy.unknownFields = copy.unknownFields
    }

    private data class BuilderCopy(
        {{- range $index, $_ := .NonOneOfFields }}
        var {{ name . }}: {{ type . }} = {{ default . }},{{ end }}
        {{- range $index, $_ := .OneOfs }}
        var {{ .Descriptor.Name }}: {{ name .Message }}.OneOf{{ upperCamel .Descriptor.Name }}? = null,{{ end }}
        var unknownFields: ByteArray? = null
    )

    private val builderCopy = BuilderCopy()

    fun build(): {{ name . }} = {{ name . }}(
    {{- range $index, $_ := .OneOfs }}
        builderCopy.{{ name . }},{{ end }}
    {{- range $index, $_ := .NonOneOfFields }}
        builderCopy.{{ name . }},{{ end }}
        builderCopy.unknownFields
    )
    {{ range .OneOfs }}

    val {{ name . }} = OneOf{{ name . }}Builder()

    inner class OneOf{{ name . }}Builder {
        {{- range .Fields }}
        var {{ name . }}: {{ typeNonNull . }}?
            set(value) {
                builderCopy.{{ .OneOf.Descriptor.Name }} = value?.let { {{ name .OneOf.Message }}.OneOf{{ upperCamel .OneOf.Descriptor.Name }}.{{ name . }}(it) }
            }
    
            get() = (builderCopy.{{ .OneOf.Descriptor.Name }} as? {{ name .OneOf.Message }}.OneOf{{ upperCamel .OneOf.Descriptor.Name }}.{{ name . }})?.value
            {{ if .Type.IsEmbed }}
            fun {{ name . }}(builder: {{ .Type.Embed | builderName }}.() -> Unit) {
                builderCopy.{{ .OneOf.Descriptor.Name }} = builderCopy.{{ .OneOf.Descriptor.Name }} ?: ({{ .Type.Embed | builderName }}().build()).copy(builder).let { {{ name .OneOf.Message }}.OneOf{{ upperCamel .OneOf.Descriptor.Name }}.{{ name . }}(it) }
            }{{ end }}
        {{ end }}
    }

    // fun {{ name . }}(builder: OneOf{{ name . }}Builder.() -> Unit) {
    //     OneOf{{ name . }}Builder().apply(builder)
    // }
    {{ end }}
    {{ range .NonOneOfFields }}
    var {{ name . }}: {{ type . }}
        set(value) {
            builderCopy.{{ name . }} = value
        }

        get() = builderCopy.{{ name . }}
    {{ if .Type.IsEmbed }}
    fun {{ name . }}(builder: {{ builderName .Type.Embed }}.() -> Unit) {
        builderCopy.{{ name . }} = (builderCopy.{{ name . }} ?: {{ builderName .Type.Embed }}().build()).copy(builder)
    }{{ else if .Type.IsMap }}
    
    inner class {{ name . }}MapBuilder {

        {{- if .Type.Element.IsEmbed }}
        infix fun {{ elTypeName .Type.Key }}.to(value: {{ name .Type.Element.Embed }}) {
            builderCopy.{{ name . }} = builderCopy.{{ name . }} + Pair(this, value)
        }
        {{ end -}}

        fun {{ .Type.Element.Embed.Name.LowerCamelCase.String }}(key: {{ elTypeName .Type.Key }}, builder: {{ builderName .Type.Element.Embed }}.() -> Unit) {
            key to {{ builderName .Type.Element.Embed }}().apply(builder).build()
        }
    }

    inline fun {{ name . }}(builder: {{ name . }}MapBuilder.() -> Unit) {
        {{ name . }}MapBuilder().apply(builder)
    }{{ else if .Type.IsRepeated }}
    
    inner class {{ name . }}ListBuilder {
        {{- if .Type.Element.IsEmbed }}
        fun add(value: {{ name .Type.Element.Embed }}) {
            builderCopy.{{ name . }} = builderCopy.{{ name . }} + value
        }

        fun add{{ simpleName .Type.Element.Embed }}(builder: {{ builderName .Type.Element.Embed }}.() -> Unit) {
            add({{ builderName .Type.Element.Embed }}().apply(builder).build())
        }{{ end }}
    }

    fun {{ name . }}(builder: {{ name . }}ListBuilder.() -> Unit) {
        {{ name . }}ListBuilder().apply(builder)
    }{{ end }}
    {{ end }}
}
{{ end }}
{{ range .AllMessages }}{{ template "message" . }}{{ end -}}`
