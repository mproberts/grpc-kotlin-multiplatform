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
	).Render()
}

const decoderTpl = `package {{ package . }}
{{ range imports . }}
import {{ . }}
import {{ fullyQualifiedName . }}.Companion.readFrom{{ end }}
import dev.mpr.grpc.protobuf.tools.ProtobufReader
{{- define "message" }}
fun {{ fullyQualifiedName . }}.Companion.readFrom(reader: ProtobufReader) = build {
    while (reader.nextField()) {
        when (reader.currentFieldNumber) {
	    {{ range .OneOfs }}
	        {{- range .Fields }}
	        {{- if .Type.IsEmbed }}
	        {{ .Descriptor.Number }} -> {
	            {{ .OneOf.Descriptor.Name }} {
	                {{ name . }} = reader.readField { fieldReader ->
	                    {{ name .Type.Embed }}.readFrom(fieldReader)
	                }
                }
            }
            {{ else if .Type.IsEnum }}
            {{ else if isBytes . }}
	        {{- else }}
	        {{ .Descriptor.Number }} -> {
	            {{ .OneOf.Descriptor.Name }} {
                    {{ name . }} = reader.{{ readerMethod .Type.ProtoType }}()
                }
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
            {{ else if .Type.IsRepeated }}
            {{ else if .Type.IsEnum }}
            {{ else if isBytes . }}
            {{ else }}{{ .Descriptor.Number }} -> { {{ name . }} = reader.{{ readerMethod .Type.ProtoType }}() }
            {{ end }}
        {{- end }}
        }
    }
}
{{ end }}
{{ range .AllMessages }}{{ template "message" . }}{{ end -}}
`

const encoderTpl = `package {{ package . }}
{{ range imports . }}
import {{ . }}
import {{ . }}.writeTo{{ end }}
import dev.mpr.grpc.protobuf.tools.ProtobufWriter
{{- define "message" }}
fun {{ fullyQualifiedName . }}.writeTo(writer: ProtobufWriter) {
    {{ range .OneOfs }}
    when ({{ .Descriptor.Name }}) {
        {{- range .Fields }}
        {{- if .Type.IsEmbed }}
        is {{ name .Message }}.OneOf{{ upperCamel .OneOf.Descriptor.Name }}.{{ name . }} -> writer.encode({{ .Descriptor.Number }}) { {{ .OneOf.Descriptor.Name }}.{{ name . }}.writeTo(this) }
        {{- else }}
        is {{ name .Message }}.OneOf{{ upperCamel .OneOf.Descriptor.Name }}.{{ name . }} -> writer.encode({{ .Descriptor.Number }}, {{ .OneOf.Descriptor.Name }}.{{ name . }})
        {{- end }}
        {{ end }}
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
    sealed class OneOf{{ upperCamel .Descriptor.Name }} {
        {{- range .Fields }}
        data class {{ name . }}(val {{ name . }}: {{ typeNonNull . }}) : OneOf{{ upperCamel .OneOf.Descriptor.Name }}(){{ end }}
    }
    {{ end }}
    companion object {
        fun build(builder: {{ builderName . }}.() -> Unit): {{ name . }} {
            return {{ builderName . }}().apply(builder).build()
        }
    }

    fun copyBuild(builder: {{ builderName . }}.() -> Unit): {{ name . }} {
        return {{ builderName . }}(this).apply(builder).build()
    }
}
{{ end }}
{{ range .Messages }}{{ template "message" . }}{{ end -}}
{{ range .Enums }}{{ template "enum" .}}{{ end -}}`

const builderTpl = `package {{ package . }}
{{ range imports . }}
import {{ . }}
import {{ . }}Builder{{ end }}
{{- define "message" }}

class {{ builderName . }} {
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
    
    inner class OneOf{{ name . }}Builder {
        {{- range .Fields }}
        var {{ name . }}: {{ typeNonNull . }}?
            set(value) {
                builderCopy.{{ .OneOf.Descriptor.Name }} = value?.let { {{ name .OneOf.Message }}.OneOf{{ upperCamel .OneOf.Descriptor.Name }}.{{ name . }}(it) }
            }

            get() = (builderCopy.{{ .OneOf.Descriptor.Name }} as? {{ name .OneOf.Message }}.OneOf{{ upperCamel .OneOf.Descriptor.Name }}.{{ name . }})?.{{ name . }}
            {{ if .Type.IsEmbed }}
            fun {{ name . }}(builder: {{ .Type.Embed | builderName }}.() -> Unit) {
                builderCopy.{{ .OneOf.Descriptor.Name }} = {{ .Type.Embed | builderName }}().apply(builder).build().let { {{ name .OneOf.Message }}.OneOf{{ upperCamel .OneOf.Descriptor.Name }}.{{ name . }}(it) }
            }{{ end }}
        {{ end }}
    }

    fun {{ name . }}(builder: OneOf{{ name . }}Builder.() -> Unit) {
        OneOf{{ name . }}Builder().apply(builder)
    }
    {{ end }}
    {{ range .NonOneOfFields }}
    var {{ name . }}: {{ type . }}
        set(value) {
            builderCopy.{{ name . }} = value
        }

        get() = builderCopy.{{ name . }}
    {{ if .Type.IsEmbed }}
    fun {{ name . }}(builder: {{ builderName .Type.Embed }}.() -> Unit) {
        builderCopy.{{ name . }} = {{ builderName .Type.Embed }}().apply(builder).build()
    }{{ else if .Type.IsMap }}
    
    inner class {{ name . }}MapBuilder {
        infix fun {{ elTypeName .Type.Key }}.to(value: {{ name .Type.Element.Embed }}) {
            builderCopy.{{ name . }} = builderCopy.{{ name . }} + Pair(this, value)
        }

        fun {{ .Type.Element.Embed.Name.LowerCamelCase.String }}(key: {{ elTypeName .Type.Key }}, builder: {{ builderName .Type.Element.Embed }}.() -> Unit) {
            key to {{ builderName .Type.Element.Embed }}().apply(builder).build()
        }
    }

    fun {{ name . }}(builder: {{ name . }}MapBuilder.() -> Unit) {
        {{ name . }}MapBuilder().apply(builder)
    }{{ else if .Type.IsRepeated }}
    
    inner class {{ name . }}ListBuilder {
        fun add(value: {{ name .Type.Element.Embed }}) {
            builderCopy.{{ name . }} = builderCopy.{{ name . }} + value
        }
        {{ if .Type.Element.IsEmbed }}
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
