package pgskt

import (
	"regexp"
	"strings"

	pgs "github.com/lyft/protoc-gen-star"
)

var nonAlphaNumPattern = regexp.MustCompile("[^a-zA-Z0-9]")

func (c context) PackageName(node pgs.Node) pgs.Name {
	e, ok := node.(pgs.Entity)
	if !ok {
		e = node.(pgs.Package).Files()[0]
	}

	// use import_path parameter ONLY if there is no go_package option in the file.
	return pgs.Name(e.File().Descriptor().GetOptions().GetJavaPackage())
}

func (c context) ImportPath(e pgs.Entity) pgs.FilePath {
	path, _ := c.optionPackage(e)
	path = c.p.Str("import_prefix") + path
	return pgs.FilePath(path)
}

func (c context) OutputPath(e pgs.Entity) pgs.FilePath {
	out := e.File().InputPath().SetExt("")
	path := strings.Join(strings.Split(c.PackageName(e).String(), "."), "/")

	// Import relative ignores the existing file structure
	return pgs.FilePath(path).Push(out.Base())
}

func (c context) optionPackage(e pgs.Entity) (path, pkg string) {
	// M mapping param overrides everything IFF the entity is not a build target
	if override, ok := c.p["M"+e.File().InputPath().String()]; ok && !e.BuildTarget() {
		path = override
		pkg = override
		if idx := strings.LastIndex(pkg, "/"); idx > -1 {
			pkg = pkg[idx+1:]
		}
		return
	}

	// check if there's a go_package option specified
	pkg = c.resolveGoPackageOption(e)
	path = e.File().InputPath().Dir().String()

	if pkg == "" {
		// have a proto package name, so use that
		if n := e.Package().ProtoName(); n != "" {
			pkg = n.SnakeCase().String()
		} else { // no other info, then replace all non-alphanumerics from the input file name
			pkg = nonAlphaNumPattern.ReplaceAllString(e.File().InputPath().BaseName(), "_")
		}
		return
	}

	// go_package="example.com/foo/bar;baz" should have a package name of `baz`
	if idx := strings.LastIndex(pkg, ";"); idx > -1 {
		path = pkg[:idx]
		pkg = nonAlphaNumPattern.ReplaceAllString(pkg[idx+1:], "_")
		return
	}

	// go_package="example.com/foo/bar" should have a package name of `bar`
	if idx := strings.LastIndex(pkg, "/"); idx > -1 {
		path = pkg
		pkg = nonAlphaNumPattern.ReplaceAllString(pkg[idx+1:], "_")
		return
	}

	pkg = nonAlphaNumPattern.ReplaceAllString(pkg, "_")

	return
}

func (c context) resolveGoPackageOption(e pgs.Entity) string {
	// attempt to get it from the current file
	if pkg := e.File().Descriptor().GetOptions().GetGoPackage(); pkg != "" {
		return pkg
	}

	// protoc-gen-go will use the go_package option from _any_ file in the same
	// execution since it's assumed that all the files are in the same Go
	// package. PG* will only verify this against files in the same proto package
	for _, f := range e.Package().Files() {
		if pkg := f.Descriptor().GetOptions().GetGoPackage(); pkg != "" {
			return pkg
		}
	}

	return ""
}
