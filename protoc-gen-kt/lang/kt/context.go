package pgskt

import pgs "github.com/lyft/protoc-gen-star"

// Context resolves Go-specific language for Packages & Entities generated by
// protoc-gen-go. The rules that drive the naming behavior are complicated, and
// result from an interplay of the go_package file option, the proto package,
// and the proto filename itself. Therefore, it is recommended that all proto
// files that are targeting Go should include a fully qualified go_package
// option. These must be consistent for all proto files that are intended to be
// in the same Go package.
type Context interface {
	// Params returns the Parameters associated with this context.
	Params() pgs.Parameters

	// Name returns the name of a Node as it would appear in the generation output
	// of protoc-gen-go. For each type, the following is returned:
	//
	//     - Package: the Go package name
	//     - File: the Go package name
	//     - Message: the struct name
	//     - Field: the field name on the Message struct
	//     - OneOf: the field name on the Message struct
	//     - Enum: the type name
	//     - EnumValue: the constant name
	//     - Service: the server interface name
	//     - Method: the method name on the server and client interface
	//
	Name(node pgs.Node) pgs.Name

	SimpleName(node pgs.Node) pgs.Name

	FieldTypeName(ft pgs.FieldTypeElem) TypeName

	FieldTypeNameNonNull(ft pgs.FieldTypeElem) TypeName

	QualifiedName(node pgs.Node) pgs.Name

	ElType(fte pgs.FieldTypeElem) TypeName

	Imports(pgs.File) []string

	BuilderName(node pgs.Node) pgs.Name

	IsBytes(field pgs.Field) bool

	FullyQualifiedName(node pgs.Node) pgs.Name

	// ServerName returns the name of the server interface for the Service.
	ServerName(service pgs.Service) pgs.Name

	// ClientName returns the name of the client interface for the Service.
	ClientName(service pgs.Service) pgs.Name

	// ServerStream returns the name of the grpc.ServerStream wrapper for this
	// method. This name is only used if client or server streaming is
	// implemented for this method.
	ServerStream(method pgs.Method) pgs.Name

	// OneofOption returns the struct name that wraps a OneOf option's value. These
	// messages contain one field, matching the value returned by Name for this
	// Field.
	OneofOption(field pgs.Field) pgs.Name

	// TypeName returns the type name of a Field as it would appear in the
	// generated message struct from protoc-gen-go. Fields from imported
	// packages will be prefixed with the package name.
	Type(field pgs.Field) TypeName
	
	TypeNonNull(field pgs.Field) TypeName

	DefaultValue(field pgs.Field) string

	// PackageName returns the name of the Node's package as it would appear in
	// Go source generated by the official protoc-gen-go plugin.
	PackageName(node pgs.Node) pgs.Name

	// ImportPath returns the Go import path for an entity as it would be
	// included in an import block in a Go file. This value is only appropriate
	// for Entities imported into a target file/package.
	ImportPath(entity pgs.Entity) pgs.FilePath

	// OutputPath returns the output path relative to the plugin's output destination
	OutputPath(entity pgs.Entity) pgs.FilePath
}

type context struct{ p pgs.Parameters }

// InitContext configures a Context that should be used for deriving Go names
// for all Packages and Entities.
func InitContext(params pgs.Parameters) Context {
	return context{params}
}

func (c context) Params() pgs.Parameters { return c.p }
