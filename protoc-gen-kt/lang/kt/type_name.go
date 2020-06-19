package pgskt

import (
	"fmt"
	"strings"

	pgs "github.com/lyft/protoc-gen-star"
)

func (c context) TypeNonNull(f pgs.Field) TypeName {
	ft := f.Type()

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
		t = c.importableTypeName(f, ft.Enum())
	default:
		t = scalarType(ft.ProtoType())
	}

	return t
}

func (c context) Type(f pgs.Field) TypeName {
	ft := f.Type()

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
		t = c.importableTypeName(f, ft.Enum())
	default:
		t = scalarType(ft.ProtoType())
	}

	return t
}

func (c context) importableTypeName(f pgs.Field, e pgs.Entity) TypeName {
	t := TypeName(c.Name(e))

	if c.ImportPath(e) == c.ImportPath(f) {
		return t
	}

	return TypeName(fmt.Sprintf("%s.%s", c.PackageName(e), t))
}

func (c context) qualifiedElType(ft pgs.FieldType) TypeName {
	el := ft.Element()
	switch {
	case el.IsEnum():
		return TypeName(c.PackageName(el.Enum()).String() + "." + c.Name(el.Enum()).String())
	case el.IsEmbed():
		return TypeName(c.PackageName(el.Embed()).String() + "." + c.Name(el.Embed()).String()).Pointer()
	default:
		return scalarType(el.ProtoType())
	}
}

func (c context) ElType(el pgs.FieldTypeElem) TypeName {
	switch {
	case el.IsEnum():
		return c.importableTypeName(el.ParentType().Field(), el.Enum())
	case el.IsEmbed():
		return TypeName(c.Name(el.Embed()))
	default:
		return scalarType(el.ProtoType())
	}
}

func (c context) elType(ft pgs.FieldType) TypeName {
	el := ft.Element()
	switch {
	case el.IsEnum():
		return c.importableTypeName(ft.Field(), el.Enum())
	case el.IsEmbed():
		return TypeName(c.Name(el.Embed()))
	default:
		return scalarType(el.ProtoType())
	}
}

func scalarType(t pgs.ProtoType) TypeName {
	switch t {
	case pgs.DoubleT:
		return "Double"
	case pgs.FloatT:
		return "Float"
	case pgs.Int64T, pgs.SFixed64, pgs.SInt64:
		return "Long"
	case pgs.UInt64T, pgs.Fixed64T:
		return "ULong"
	case pgs.Int32T, pgs.SFixed32, pgs.SInt32:
		return "Int"
	case pgs.UInt32T, pgs.Fixed32T:
		return "UInt"
	case pgs.BoolT:
		return "Boolean"
	case pgs.StringT:
		return "String"
	case pgs.BytesT:
		return "ByteArray"
	default:
		panic("unreachable: invalid scalar type")
	}
}

// A TypeName describes the name of a type (type on a field, or method signature)
type TypeName string

// String satisfies the strings.Stringer interface.
func (n TypeName) String() string { return string(n) }

// Element returns the TypeName of the element of n. For types other than
// slices and maps, this just returns n.
func (n TypeName) Element() TypeName {
	parts := strings.SplitN(string(n), "]", 2)
	return TypeName(parts[len(parts)-1])
}

// Key returns the TypeName of the key of n. For slices, the return TypeName is
// always "int", and for non slice/map types an empty TypeName is returned.
func (n TypeName) Key() TypeName {
	parts := strings.SplitN(string(n), "]", 2)
	if len(parts) == 1 {
		return TypeName("")
	}

	parts = strings.SplitN(parts[0], "[", 2)
	if len(parts) != 2 {
		return TypeName("")
	} else if parts[1] == "" {
		return TypeName("int")
	}

	return TypeName(parts[1])
}

// Pointer converts TypeName n to it's pointer type. If n is already a pointer,
// slice, or map, it is returned unmodified.
func (n TypeName) Pointer() TypeName {
	ns := string(n)
	if strings.HasPrefix(ns, "*") ||
		strings.HasPrefix(ns, "[") ||
		strings.HasPrefix(ns, "map[") {
		return n
	}

	return TypeName(ns)
}

// Value converts TypeName n to it's value type. If n is already a value type,
// slice, or map it is returned unmodified.
func (n TypeName) Value() TypeName {
	return TypeName(strings.TrimPrefix(string(n), "*"))
}
