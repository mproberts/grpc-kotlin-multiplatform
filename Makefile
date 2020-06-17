.PHONY: test
deps:
	@go install github.com/mproberts/grpc-kotlin-multiplatform/protoc-gen-kt

test: deps
	@$(foreach f,$(shell find ../examples -type f -name "*.proto"), \
		protoc -I ../examples --kt_out=../lib/src/commonMain/kotlin/ $(f); \
	)

wellknown: deps
# 	-@go get github.com/protocolbuffers/protobuf

	@$(foreach f,any.proto duration.proto empty.proto struct.proto wrappers.proto timestamp.proto, \
		protoc -I ../../protocolbuffers/protobuf/src/google/protobuf/ --kt_out=./lib/src/commonMain/kotlin/ ../../protocolbuffers/protobuf/src/google/protobuf/$(f); \
	)
