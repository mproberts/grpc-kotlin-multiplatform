package pgskt

import (
	"fmt"
	"strings"
	"unicode"
	"unicode/utf8"

	"github.com/golang/protobuf/protoc-gen-go/generator"
	pgs "github.com/lyft/protoc-gen-star"
)

func uniqueStrings(list []string) []string {
	var unique []string

	for _, s := range list {
		skip := false

		for _, u := range unique {
			if u == s {
				skip = true
				break
			}
		}

		if !skip {
			unique = append(unique, s)
		}
	}

	return unique
}

func (c context) Imports(f pgs.File) []string {
	var imports []string

	for _, msg := range f.AllMessages() {
		for _, field := range msg.Fields() {
			if field.Type().IsEmbed() {
				fieldTypeName := c.FullyQualifiedName(field.Type().Embed()).String()

				if !strings.HasPrefix(fieldTypeName, c.PackageName(f).String()) {
					imports = append(imports, fieldTypeName)
				}
			} else if field.Type().IsMap() {
				fieldTypeName := c.qualifiedElType(field.Type()).String()

				if !strings.HasPrefix(fieldTypeName, c.PackageName(f).String()) {
					imports = append(imports, fieldTypeName)
				}
			} else if field.Type().IsRepeated() {
				if field.Type().Element().IsEmbed() {
					fieldTypeName := c.qualifiedElType(field.Type()).String()

					if !strings.HasPrefix(fieldTypeName, c.PackageName(f).String()) {
						imports = append(imports, fieldTypeName)
					}
				}
			} else if field.Type().IsEnum() {
			}
		}
	}

	return uniqueStrings(imports)
}

func (c context) PackageImports(f pgs.File) []string {
	var imports []string

	for _, msg := range f.AllMessages() {
		for _, field := range msg.Fields() {
			if field.Type().IsEmbed() {
				fieldTypeName := c.FullyQualifiedName(field.Type().Embed()).String()

				if !strings.HasPrefix(fieldTypeName, c.PackageName(f).String()) {
					imports = append(imports, c.PackageName(field.Type().Embed()).String())
				}
			} else if field.Type().IsMap() {
				fieldTypeName := c.qualifiedElType(field.Type()).String()

				if !strings.HasPrefix(fieldTypeName, c.PackageName(f).String()) {
					imports = append(imports, c.StripLastSegment(c.qualifiedElType(field.Type()).String()))
				}
			} else if field.Type().IsRepeated() {
				if field.Type().Element().IsEmbed() {
					fieldTypeName := c.qualifiedElType(field.Type()).String()

					if !strings.HasPrefix(fieldTypeName, c.PackageName(f).String()) {
						imports = append(imports, c.StripLastSegment(c.qualifiedElType(field.Type()).String()))
					}
				}
			} else if field.Type().IsEnum() {
			}
		}
	}

	return uniqueStrings(imports)
}

/*
func (c context) PackageImports(f pgs.File) []string {
	var imports []string

	for _, msg := range f.AllMessages() {
		for _, field := range msg.Fields() {
			if field.Type().IsEmbed() {
				packageName := c.PackageName(field.Type().Embed()).String()
				fieldTypeName := c.FullyQualifiedName(field.Type().Embed()).String()

				if !strings.HasPrefix(fieldTypeName, c.PackageName(f).String()) {
					imports = append(imports, packageName)
				}
			} else if field.Type().IsEnum() {
			}
		}
	}

	return uniqueStrings(imports)
}
*/
func (c context) BuilderImports(f pgs.File) []string {
	var imports []string

	for _, msg := range f.AllMessages() {
		for _, field := range msg.Fields() {
			if field.Type().IsEmbed() {
				packageName := c.PackageName(field.Type().Embed()).String()
				fieldTypeName := c.FullyQualifiedName(field.Type().Embed()).String()

				if !strings.HasPrefix(fieldTypeName, c.PackageName(f).String()) {
					imports = append(imports, packageName+"."+c.SimpleBuilderName(field.Type().Embed()).String())
				}
			}
		}
	}

	return uniqueStrings(imports)
}

func (c context) SimpleName(node pgs.Node) pgs.Name {
	switch en := node.(type) {
	case pgs.Message:
		return replaceProtected(PGGUpperCamelCase(en.Name()))
	case pgs.Enum:
		return replaceProtected(PGGUpperCamelCase(en.Name()))
	default:
		panic("unknown type")
	}
}

func (c context) OriginalName(node pgs.Node) pgs.Name {
	switch en := node.(type) {
	case pgs.Message:
		return en.Name()
	case pgs.Enum:
		return en.Name()
	case pgs.Entity:
		return en.Name()
	default:
		panic("unknown type")
	}
}

func (c context) StripLastSegment(something string) string {
	remainder := ""

	components := strings.Split(something, ".")
	components = components[:len(components)-1]

	for _, component := range components {
		if len(remainder) > 0 {
			remainder = remainder + "." + component
		} else {
			remainder = component
		}
	}

	return remainder
}

func (c context) BuilderName(node pgs.Node) pgs.Name {
	switch node.(type) {
	case pgs.Message:
		return c.PackageName(node) + "." + pgs.Name(strings.ReplaceAll(c.Name(node).String(), ".", "_")+"Builder")
	default:
		panic("unknown type")
	}
}

func (c context) SimpleBuilderName(node pgs.Node) pgs.Name {
	return pgs.Name(strings.ReplaceAll(c.Name(node).String(), ".", "_") + "Builder")
}

func (c context) QualifiedName(node pgs.Node) pgs.Name {
	switch en := node.(type) {
	case pgs.Message:
		return pgs.Name(en.Name())
	default:
		panic("unknown type")
	}
}

func (c context) FullyQualifiedName(node pgs.Node) pgs.Name {
	switch en := node.(type) {
	case pgs.Message:
		return pgs.Name(c.PackageName(node) + "." + c.Name(en))
	default:
		panic("unknown type")
	}
}

func (c context) FullyQualifiedCompanionName(node pgs.Node) pgs.Name {
	// Message or Enum
	type ChildEntity interface {
		Name() pgs.Name
		Parent() pgs.ParentEntity
	}

	switch en := node.(type) {
	case ChildEntity: // Message or Enum types, which may be nested
		if p, ok := en.Parent().(pgs.Message); ok {
			return pgs.Name(c.Name(p) + ".Companion." + en.Name())
		}
		return PGGUpperCamelCase(en.Name())
	case pgs.Entity: // any other entity should be just upper-camel-cased
		return PGGUpperCamelCase(en.Name())
	default:
		panic("unreachable")
	}
}

func (c context) EscapedFullyQualifiedName(node pgs.Node) string {
	return strings.ReplaceAll(c.FullyQualifiedName(node).String(), ".", "__")
}

func (c context) FieldTypeNameNonNull(fte pgs.FieldTypeElem) TypeName {
	ft := fte.ParentType()

	var t TypeName
	switch {
	case ft.IsMap():
		key := scalarType(ft.Key().ProtoType())
		return TypeName(fmt.Sprintf("Map<%s, %s>", key, c.elType(ft)))
	case ft.IsRepeated():
		return TypeName(fmt.Sprintf("List<%s>", c.elType(ft)))
	case ft.IsEmbed():
		return TypeName(c.Name(ft.Embed()).String())
	case ft.IsEnum():
		t = c.importableTypeName(ft.Field(), ft.Enum())
	default:
		t = scalarType(ft.ProtoType())
	}

	return t
}

func (c context) FieldTypeName(fte pgs.FieldTypeElem) TypeName {
	ft := fte.ParentType()

	var t TypeName
	switch {
	case ft.IsMap():
		key := scalarType(ft.Key().ProtoType())
		return TypeName(fmt.Sprintf("Map<%s, %s>", key, c.elType(ft)))
	case ft.IsRepeated():
		return TypeName(fmt.Sprintf("List<%s>", c.elType(ft)))
	case ft.IsEmbed():
		return TypeName(c.Name(ft.Embed()).String() + "?")
	case ft.IsEnum():
		t = c.importableTypeName(ft.Field(), ft.Enum())
	default:
		t = scalarType(ft.ProtoType())
	}

	return t
}

func (c context) Name(node pgs.Node) pgs.Name {
	// Message or Enum
	type ChildEntity interface {
		Name() pgs.Name
		Parent() pgs.ParentEntity
	}

	switch en := node.(type) {
	case pgs.Package: // the package name for the first file (should be consistent)
		return c.PackageName(en)
	case pgs.File: // the package name for this file
		return c.PackageName(en)
	case ChildEntity: // Message or Enum types, which may be nested
		if p, ok := en.Parent().(pgs.Message); ok {
			return pgs.Name(joinChild(c.Name(p), en.Name()))
		}
		return replaceProtected(PGGUpperCamelCase(en.Name()))
	case pgs.Field: // field names cannot conflict with other generated methods
		return replaceProtected(PGGLowerCamelCase(en.Name()))
	case pgs.OneOf: // oneof field names cannot conflict with other generated methods
		return replaceProtected(PGGLowerCamelCase(en.Name()))
	case pgs.EnumValue: // EnumValue are prefixed with the enum name
		// if _, ok := en.Enum().Parent().(pgs.File); ok {
		// return pgs.Name(joinNames(c.Name(en.Enum()), en.Name()))
		// }
		// return pgs.Name(joinNames(c.Name(en.Enum().Parent()), en.Name()))

		return replaceProtected(pgs.Name(strings.ToUpper(en.Name().String())))
	case pgs.Service: // always return the server name
		return replaceProtected(PGGUpperCamelCase(en.Name()))
	case pgs.Entity: // any other entity should be just upper-camel-cased
		return replaceProtected(PGGLowerCamelCase(en.Name()))
	default:
		panic("unreachable")
	}
}

func (c context) IsBytes(field pgs.Field) bool {
	ft := field.Type()
	switch {
	case ft.IsMap():
		return false
	case ft.IsRepeated():
		return false
	case ft.IsEmbed():
		return false
	case ft.IsEnum():
		return false
	default:
		t := ft.ProtoType()
		switch t {
		case pgs.BytesT:
			return true
		default:
			return false
		}
	}

	panic("unreachable: invalid scalar type")
}

func (c context) DefaultValue(field pgs.Field) string {
	ft := field.Type()

	switch {
	case ft.IsMap():
		return "emptyMap()"
	case ft.IsRepeated():
		return "emptyList()"
	case ft.IsEmbed():
		return "null"
	case ft.IsEnum():
		return c.Type(field).String() + "." + ft.Enum().Values()[0].Name().String()
	default:
		t := ft.ProtoType()
		switch t {
		case pgs.DoubleT:
			return "0.0"
		case pgs.FloatT:
			return "0.0f"
		case pgs.Int64T, pgs.SFixed64, pgs.SInt64:
			return "0L"
		case pgs.UInt64T, pgs.Fixed64T:
			return "0UL"
		case pgs.Int32T, pgs.SFixed32, pgs.SInt32:
			return "0"
		case pgs.UInt32T, pgs.Fixed32T:
			return "0U"
		case pgs.BoolT:
			return "false"
		case pgs.StringT:
			return "\"\""
		case pgs.BytesT:
			return "ByteArray(0)"
		}
	}

	panic("unreachable: invalid scalar type")
}

func (c context) ReaderMethod(t pgs.ProtoType) string {
	switch t {
	case pgs.DoubleT:
		return "readDouble"
	case pgs.FloatT:
		return "readFloat"
	case pgs.Int64T:
		return "readInt64"
	case pgs.SInt64:
		return "readSInt64"
	case pgs.SFixed64:
		return "readFixedInt64"
	case pgs.Fixed64T:
		return "readFixedInt64"
	case pgs.UInt64T:
		return "readUInt64"
	case pgs.Int32T:
		return "readInt32"
	case pgs.SInt32:
		return "readSInt32"
	case pgs.SFixed32:
		return "readFixedInt32"
	case pgs.UInt32T:
		return "readUInt32"
	case pgs.Fixed32T:
		return "readFixedInt32"
	case pgs.BoolT:
		return "readBool"
	case pgs.StringT:
		return "readString"
	case pgs.BytesT:
		return "readBytes"
	}

	panic("unreachable: invalid scalar type")
}

func (c context) OneofOption(field pgs.Field) pgs.Name {
	n := pgs.Name(joinNames(c.Name(field.Message()), c.Name(field)))

	for _, msg := range field.Message().Messages() {
		if c.Name(msg) == n {
			return n + "."
		}
	}

	for _, en := range field.Message().Enums() {
		if c.Name(en) == n {
			return n + "."
		}
	}

	return n
}

func (c context) ServerName(s pgs.Service) pgs.Name {
	n := PGGUpperCamelCase(s.Name())
	return pgs.Name(fmt.Sprintf("%sServer", n))
}

func (c context) ClientName(s pgs.Service) pgs.Name {
	n := PGGUpperCamelCase(s.Name())
	return pgs.Name(fmt.Sprintf("%sClient", n))
}

func (c context) ServerStream(m pgs.Method) pgs.Name {
	s := PGGUpperCamelCase(m.Service().Name())
	n := PGGUpperCamelCase(m.Name())
	return joinNames(s, n) + "Server"
}

// PGGUpperCamelCase converts Name n to the protoc-gen-go defined upper
// camelcase. The rules are slightly different from pgs.UpperCamelCase in that
// leading underscores are converted to 'X', mid-string underscores followed by
// lowercase letters are removed and the letter is capitalized, all other
// punctuation is preserved. This method should be used when deriving names of
// protoc-gen-go generated code (ie, message/service struct names and field
// names).
//
// See: https://godoc.org/github.com/golang/protobuf/protoc-gen-go/generator#CamelCase
func PGGUpperCamelCase(n pgs.Name) pgs.Name {
	return pgs.Name(generator.CamelCase(n.String()))
}

// PGGLowerCamelCase converts Name n to the protoc-gen-go defined lower
// camelcase. The rules are slightly different from pgs.LowerCamelCase in that
// leading underscores are converted to 'X', mid-string underscores followed by
// lowercase letters are removed and the letter is capitalized, all other
// punctuation is preserved. This method should be used when deriving names of
// protoc-gen-go generated code (ie, message/service struct names and field
// names).
//
// See: https://godoc.org/github.com/golang/protobuf/protoc-gen-go/generator#CamelCase
func PGGLowerCamelCase(n pgs.Name) pgs.Name {
	camelCase := generator.CamelCase(n.String())

	return pgs.Name(strings.ToLower(camelCase[0:1]) + camelCase[1:])
}

var protectedNames = map[pgs.Name]pgs.Name{
	"as":        "as_",
	"break":     "break_",
	"class":     "class_",
	"continue":  "continue_",
	"do":        "do_",
	"else":      "else_",
	"false":     "false_",
	"for":       "for_",
	"fun":       "fun_",
	"if":        "if_",
	"in":        "in_",
	"interface": "interface_",
	"is":        "is_",
	"null":      "null_",
	"object":    "object_",
	"package":   "package_",
	"return":    "return_",
	"super":     "super_",
	"this":      "this_",
	"throw":     "throw_",
	"true":      "true_",
	"try":       "try_",
	"typealias": "typealias_",
	"typeof":    "typeof_",
	"val":       "val_",
	"var":       "var_",
	"when":      "when_",
	"while":     "while_",
	"Any":       "PbAny",
}

func replaceProtected(n pgs.Name) pgs.Name {
	if use, protected := protectedNames[n]; protected {
		return use
	}
	return n
}

func joinChild(a, b pgs.Name) pgs.Name {
	if r, _ := utf8.DecodeRuneInString(b.String()); unicode.IsLetter(r) && unicode.IsLower(r) {
		return pgs.Name(fmt.Sprintf("%s%s", a, PGGUpperCamelCase(b)))
	}
	return joinNames(a, PGGUpperCamelCase(b))
}

func joinNames(a, b pgs.Name) pgs.Name {
	return pgs.Name(fmt.Sprintf("%s.%s", a, b))
}
