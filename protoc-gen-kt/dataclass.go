package main

import (
	"text/template"

	"bytes"

	"github.com/Masterminds/sprig"

	// validate "github.com/envoyproxy/protoc-gen-validate/validate"

	pgskt "github.com/mproberts/grpc-kotlin-multiplatform/protoc-gen-kt/lang/kt"
	pgs "github.com/lyft/protoc-gen-star"
)

type TemplateGenPlugin struct {
	*pgs.ModuleBase
	templateString string
	ext string
	ctx pgskt.Context
	tpl *template.Template
}

func RenderTemplate(template string, ext string) *TemplateGenPlugin { return &TemplateGenPlugin{ModuleBase: &pgs.ModuleBase{}, templateString: template, ext: ext} }

func (p *TemplateGenPlugin) InitContext(c pgs.BuildContext) {
	p.ModuleBase.InitContext(c)
	p.ctx = pgskt.InitContext(c.Parameters())

	tpl := template.New("root")

	funcs := sprig.TxtFuncMap()

	funcs["builderName"] = p.ctx.BuilderName
	funcs["simpleName"] = p.ctx.SimpleName
	funcs["qualifiedName"] = p.ctx.QualifiedName
	funcs["fullyQualifiedName"] = p.ctx.FullyQualifiedName
	funcs["package"] = p.ctx.PackageName
	funcs["name"] = p.ctx.Name
	funcs["fieldTypeName"] = p.ctx.FieldTypeName
	funcs["type"] = p.ctx.Type
	funcs["isBytes"] = p.ctx.IsBytes
	funcs["imports"] = p.ctx.Imports
	funcs["default"] = p.ctx.DefaultValue
	funcs["include"] = func(name string, data interface{}) (string, error) {
		buf := bytes.NewBuffer(nil)
		if err := tpl.ExecuteTemplate(buf, name, data); err != nil {
			return "", err
		}
		return buf.String(), nil
	}

	tpl = tpl.Funcs(funcs)

	p.tpl = template.Must(tpl.Parse(p.templateString))
}

func (p *TemplateGenPlugin) Name() string { return p.ext }

func (p *TemplateGenPlugin) Execute(targets map[string]pgs.File, pkgs map[string]pgs.Package) []pgs.Artifact {

	for _, t := range targets {
		p.generate(t)
	}

	return p.Artifacts()
}

func (p *TemplateGenPlugin) generate(f pgs.File) {
	if len(f.Messages()) == 0 {
		return
	}

	name := p.ctx.OutputPath(f).SetExt(p.ext)

	p.AddGeneratorTemplateFile(name.String(), p.tpl, f)
}
