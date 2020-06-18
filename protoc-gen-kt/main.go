package main

import (
	pgs "github.com/lyft/protoc-gen-star"
)

func main() {
	pgs.Init(
		pgs.DebugEnv("DEBUG"),
	).RegisterModule(
		RenderTemplate(encoderTpl, ".encode.kt"),
		RenderTemplate(dataClassTpl, ".data.kt"),
		RenderTemplate(builderTpl, ".builder.kt"),
	).Render()
}

const encoderTpl = `package {{ package . }}
{{ range imports . }}
import {{ . }}
import {{ . }}.writeTo{{ end }}
import dev.mpr.grpc.protobuf.tools.ProtobufWriter
{{- define "message" }}
fun {{ simpleName . }}.writeTo(writer: ProtobufWriter) {
    {{- range $index, $_ := .Fields }}
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
{{ range .Messages }}{{ template "message" . }}{{ end -}}`

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
    {{- range $index, $_ := .Fields }}
    val {{ name . }}: {{ type . }} = {{ default . }},{{ end }}
    val unknownFields: ByteArray? = null
) {
    {{- range .Messages }}{{ include "message" . | indent 4 }}{{ end }}
    {{- range .Enums }}{{ include "enum" . | indent 4 }}{{ end }}
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
import dev.mpr.grpc.ProtoDsl
{{- define "message" }}
@ProtoDsl
class {{ builderName . }} {
	constructor()

	constructor(copy: {{ name . }}) {
	    {{- range $index, $_ := .Fields }}
	    builderCopy.{{ name . }} = copy.{{ name . }}{{ end }}
	    builderCopy.unknownFields = copy.unknownFields
	}

	private object builderCopy {
	    {{- range $index, $_ := .Fields }}
	    var {{ name . }}: {{ type . }} = {{ default . }}{{ end }}
	    var unknownFields: ByteArray? = null
	}

	fun build(): {{ name . }} = {{ name . }}(
    {{- range $index, $_ := .Fields }}
    	builderCopy.{{ name . }},{{ end }}
    	builderCopy.unknownFields
	)
	{{ range .Fields }}
	var {{ name . }}: {{ type . }}
		set(value) {
			builderCopy.{{ name . }} = value
		}

		get() = builderCopy.{{ name . }}
	{{ if .Type.IsEmbed }}
	fun {{ name . }}(builder: {{ .Type.Embed | builderName }}.() -> Unit) {
		builderCopy.{{ name . }} = {{ .Type.Embed | builderName }}().apply(builder).build()
	}{{ else if .Type.IsMap }}
	@ProtoDsl
	inner class {{ name . }}MapBuilder {
		infix fun {{ fieldTypeName .Type.Key }}.to(builder: {{ builderName .Type.Element.Embed }}.() -> Unit) {
			builderCopy.{{ name . }} = builderCopy.{{ name . }} + Pair(this, {{ builderName .Type.Element.Embed }}().apply(builder).build())
		}

		fun {{ name . }}(key: {{ fieldTypeName .Type.Key }}, builder: {{ builderName .Type.Element.Embed }}.() -> Unit) {
			key to {{ builderName .Type.Element.Embed }}().apply(builder).build()
		}
	}

	fun {{ name . }}(builder: {{ name . }}MapBuilder.() -> Unit) {
		{{ name . }}MapBuilder().apply(builder)
	}{{ else if .Type.IsRepeated }}
	@ProtoDsl
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
