/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.codegen.poet.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.security.InvalidParameterException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;
import software.amazon.awssdk.codegen.model.intermediate.IntermediateModel;
import software.amazon.awssdk.codegen.model.intermediate.MemberModel;
import software.amazon.awssdk.codegen.model.intermediate.OperationModel;
import software.amazon.awssdk.codegen.model.intermediate.ShapeModel;
import software.amazon.awssdk.codegen.model.service.PaginatorDefinition;
import software.amazon.awssdk.codegen.poet.ClassSpec;
import software.amazon.awssdk.codegen.poet.PoetExtensions;
import software.amazon.awssdk.codegen.poet.PoetUtils;
import software.amazon.awssdk.pagination.NextPageSupplier;
import software.amazon.awssdk.pagination.Paginated;
import software.amazon.awssdk.pagination.PaginatedItemsIterable;
import software.amazon.awssdk.pagination.PaginatedResponsesIterator;
import software.amazon.awssdk.pagination.SdkIterable;

/**
 * Java poet {@link ClassSpec} to generate the response class for sync paginated operations.
 *
 * Sample of a generated class with annotations:

    public final class ListTablesPaginator implements Paginated&lt;ListTablesResponse, String> {
        private final DynamoDBClient client;

        private final ListTablesRequest firstRequest;

        private final ListTablesResponse firstResponsePage;

        private final NextPageSupplier nextPageSupplier;

        public ListTablesPaginator(final DynamoDBClient client, final ListTablesRequest firstRequest,
            final ListTablesResponse firstResponsePage) {
            this.client = client;
            this.firstRequest = firstRequest;
            this.firstResponsePage = firstResponsePage;
            this.nextPageSupplier = new ListTablesResponseSupplier();
        }

        public ListTablesResponse firstPage() {
            return firstResponsePage;
        }

        public Iterator&lt;ListTablesResponse> iterator() {
            return new PaginatedResponsesIterator(firstResponsePage, nextPageSupplier);
        }

        public SdkIterable&lt;String> tableNames() {
            Function&lt;ListTablesResponse, Iterator&lt;String>> getPaginatedMemberIterator = response -> response != null
                            ? response.tableNames().iterator() : null;

            return new PaginatedItemsIterable(this, getPaginatedMemberIterator);
        }

        @Override
        public SdkIterable&lt;String> allItems() {
            return tableNames();
        }

        private class ListTablesResponseSupplier implements NextPageSupplier&lt;ListTablesResponse> {
            @Override
            public ListTablesResponse nextPage(ListTablesResponse currentPage) {
                if (currentPage == null || currentPage.lastEvaluatedTableName() == null) {
                    return null;
                } else {
                    return client.listTables(firstRequest.toBuilder()
                                .exclusiveStartTableName(currentPage.lastEvaluatedTableName())
                                .build());
                }
            }
        }
    }
 */
public class PaginatorResponseClassSpec implements ClassSpec {

    private static final String CLIENT_MEMBER = "client";
    private static final String REQUEST_MEMBER = "firstRequest";
    private static final String RESPONSE_MEMBER = "firstResponsePage";
    private static final String NEXT_PAGE_SUPPLIER_MEMBER = "nextPageSupplier";

    private static final String ALL_ITEMS_METHOD = "allItems";
    private static final String FIRST_PAGE_METHOD = "firstPage";
    private static final String NEXT_PAGE_METHOD = "nextPage";

    private final IntermediateModel model;
    private final PoetExtensions poetExtensions;
    private final TypeProvider typeProvider;
    private final String c2jOperationName;
    private final PaginatorDefinition paginatorDefinition;
    private final OperationModel operationModel;

    public PaginatorResponseClassSpec(IntermediateModel intermediateModel,
                                      String c2jOperationName,
                                      PaginatorDefinition paginatorDefinition) {

        this.model = intermediateModel;
        this.poetExtensions = new PoetExtensions(intermediateModel);
        this.typeProvider = new TypeProvider(intermediateModel);
        this.c2jOperationName = c2jOperationName;
        this.paginatorDefinition = paginatorDefinition;
        this.operationModel = model.getOperation(c2jOperationName);
    }

    @Override
    public TypeSpec poetSpec() {
        TypeSpec.Builder specBuilder = TypeSpec.classBuilder(className())
                                               .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                                               .addAnnotation(PoetUtils.GENERATED)
                                               .addSuperinterface(getPaginatedInterface())
                                               .addFields(Stream.of(syncClientInterfaceField(),
                                                                    requestClassField(),
                                                                    responseClassField(),
                                                                    nextPageSupplierField())
                                                                .collect(Collectors.toList()))
                                               .addMethod(constructor())
                                               .addMethod(firstPageMethod())
                                               .addMethod(iteratorMethod())
                                               .addMethods(getMethodSpecsForResultKeyList())
                                               .addMethod(allItemsMethod())
                                               .addType(nextPageSupplierClass());

        return specBuilder.build();
    }

    @Override
    public ClassName className() {
        return poetExtensions.getResponseClassForPaginatedSyncOperation(c2jOperationName);
    }

    /**
     * Returns the interface that is implemented by the Paginated Response class.
     */
    private TypeName getPaginatedInterface() {
        // TODO using first element in the result_key list for now. Needs to add customization to choose the desired one
        return ParameterizedTypeName.get(ClassName.get(Paginated.class), responseType(),
                                         getTypeForResultKey(paginatorDefinition.getResultKey().get(0)));
    }

    /**
     * @return A Poet {@link ClassName} for the sync operation request type.
     *
     * Example: For ListTables operation, it will be "ListTablesRequest" class.
     */
    private ClassName requestType() {
        return poetExtensions.getModelClass(operationModel.getInput().getVariableType());
    }

    /**
     * @return A Poet {@link ClassName} for the sync operation response type.
     *
     * Example: For ListTables operation, it will be "ListTablesResponse" class.
     */
    private ClassName responseType() {
        return poetExtensions.getModelClass(operationModel.getReturnType().getReturnType());
    }

    /**
     * @return A Poet {@link ClassName} for the sync client interface
     */
    private ClassName getClientInterfaceName() {
        return poetExtensions.getClientClass(model.getMetadata().getSyncInterface());
    }

    private FieldSpec syncClientInterfaceField() {
        return FieldSpec.builder(getClientInterfaceName(), CLIENT_MEMBER, Modifier.PRIVATE, Modifier.FINAL).build();
    }

    private FieldSpec requestClassField() {
        return FieldSpec.builder(requestType(), REQUEST_MEMBER, Modifier.PRIVATE, Modifier.FINAL).build();
    }

    private FieldSpec responseClassField() {
        return FieldSpec.builder(responseType(), RESPONSE_MEMBER, Modifier.PRIVATE, Modifier.FINAL).build();
    }

    private FieldSpec nextPageSupplierField() {
        return FieldSpec.builder(NextPageSupplier.class, NEXT_PAGE_SUPPLIER_MEMBER, Modifier.PRIVATE, Modifier.FINAL).build();
    }

    private String nextPageSupplierClassName() {
        return operationModel.getReturnType().getReturnType() + "Supplier";
    }

    private MethodSpec constructor() {
        return MethodSpec.constructorBuilder()
                         .addModifiers(Modifier.PUBLIC)
                         .addParameter(getClientInterfaceName(), CLIENT_MEMBER, Modifier.FINAL)
                         .addParameter(requestType(), REQUEST_MEMBER, Modifier.FINAL)
                         .addParameter(responseType(), RESPONSE_MEMBER, Modifier.FINAL)
                         .addStatement("this.$L = $L", CLIENT_MEMBER, CLIENT_MEMBER)
                         .addStatement("this.$L = $L", REQUEST_MEMBER, REQUEST_MEMBER)
                         .addStatement("this.$L = $L", RESPONSE_MEMBER, RESPONSE_MEMBER)
                         .addStatement("this.$L = new $L()", NEXT_PAGE_SUPPLIER_MEMBER, nextPageSupplierClassName())
                .build();
    }

    /**
     * A {@link MethodSpec} for the firstPage() method which returns the
     * first response page for the paginated operation.
     */
    private MethodSpec firstPageMethod() {
        return MethodSpec.methodBuilder(FIRST_PAGE_METHOD)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(responseType())
                .addStatement("return $L", RESPONSE_MEMBER)
                .build();
    }

    /**
     * A {@link MethodSpec} for the overridden iterator() method which is inherited
     * from the interface.
     */
    private MethodSpec iteratorMethod() {
        return MethodSpec.methodBuilder("iterator")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(Iterator.class), responseType()))
                .addStatement("return new $T($L, $L)", PaginatedResponsesIterator.class,
                        RESPONSE_MEMBER, NEXT_PAGE_SUPPLIER_MEMBER)
                .build();
    }

    /**
     * Returns iterable of {@link MethodSpec} to generate helper methods for all members
     * in {@link PaginatorDefinition#getResultKey()}. All the generated methods return an SdkIterable.
     */
    private Iterable<MethodSpec> getMethodSpecsForResultKeyList() {
        return paginatorDefinition.getResultKey().stream()
                                  .map(this::getMethodsSpecForSingleResultKey)
                                  .collect(Collectors.toList());
    }

    /*
     * Generate a method spec for single element in {@link PaginatorDefinition#getResultKey()} list.
     *
     * If the element is "Folders" and its type is "List<FolderMetadata>", generated code looks like:
     *
     *  public SdkIterable<FolderMetadata> folders() {
     *      Function<DescribeFolderContentsResponse, Iterator<FolderMetadata>> getPaginatedMemberIterator =
     *              response -> response != null ? response.folders().iterator() : null;
     *
     *      return new PaginatedItemsIterable(this, getPaginatedMemberIterator);
     *  }
     */
    private MethodSpec getMethodsSpecForSingleResultKey(String resultKey) {
        TypeName resultKeyType = getTypeForResultKey(resultKey);
        MemberModel resultKeyModel = memberModelForResponseMember(resultKey);

        return MethodSpec.methodBuilder(resultKeyModel.getFluentGetterMethodName())
                         .addModifiers(Modifier.PUBLIC)
                         .returns(ParameterizedTypeName.get(ClassName.get(SdkIterable.class), resultKeyType))
                         .addCode("$T getIterator = ",
                                  ParameterizedTypeName.get(ClassName.get(Function.class),
                                                            responseType(),
                                                            ParameterizedTypeName.get(ClassName.get(Iterator.class),
                                                                                      resultKeyType)))
                         .addCode(getPaginatedMemberIteratorLambdaBlock(resultKey, resultKeyModel))
                         .addCode("\n")
                         .addStatement("return new $T(this, getIterator)", PaginatedItemsIterable.class)
                         .build();
    }

    private CodeBlock getPaginatedMemberIteratorLambdaBlock(String resultKey, MemberModel resultKeyModel) {
        String fluentGetter = fluentGetterMethodForResponseMember(resultKey);

        CodeBlock iteratorBlock = null;

        if (resultKeyModel.isList()) {
            iteratorBlock = CodeBlock.builder().add("response.$L.iterator()", fluentGetter).build();

        } else if (resultKeyModel.isMap()) {
            iteratorBlock = CodeBlock.builder().add("response.$L.entrySet().iterator()", fluentGetter).build();
        }

        return CodeBlock.builder().addStatement("response -> response != null ? $L : null", iteratorBlock).build();
    }


    /**
     * Returns a {@link MethodSpec} for the overridden #ALL_ITEMS_METHOD method that returns
     * an iterable for iterating through the paginated member.
     *
     * TODO
     * If there are multiple paginated members in the response, we will add customization to choose one
     * which is most likely used by customers.
     *
     * Other option is to remove the allItems() method.
     */
    private MethodSpec allItemsMethod() {

        // TODO using first element in the list for now. Needs to add customization to choose one
        TypeName mainResultKeyType = getTypeForResultKey(paginatorDefinition.getResultKey().get(0));

        return MethodSpec.methodBuilder(ALL_ITEMS_METHOD)
                         .addAnnotation(Override.class)
                         .addModifiers(Modifier.PUBLIC)
                         .returns(ParameterizedTypeName.get(ClassName.get(SdkIterable.class), mainResultKeyType))
                         .addStatement("return $L()", memberModelForResponseMember(paginatorDefinition.getResultKey().get(0))
                             .getFluentGetterMethodName())
                         .build();
    }

    /**
     * Returns a list of fluent setter method names for members in {@link PaginatorDefinition#getInputToken()} list.
     * The size of list returned by this method is equal to the size of {@link PaginatorDefinition#getInputToken()} list.
     */
    private List<String> fluentSetterMethodNamesForInputToken() {
        return paginatorDefinition.getInputToken().stream()
                                  .map(this::fluentSetterNameForSingleInputToken)
                                  .collect(Collectors.toList());
    }

    /**
     * Returns the fluent setter method name for a single member in the request.
     *
     * The values in {@link PaginatorDefinition#getInputToken()} are not nested unlike
     * {@link PaginatorDefinition#getOutputToken()}.
     */
    private String fluentSetterNameForSingleInputToken(String inputToken) {
        return operationModel.getInputShape()
                .findMemberModelByC2jName(inputToken)
                .getFluentSetterMethodName();
    }

    /**
     * Returns a list of fluent getter methods for members in {@link PaginatorDefinition#getOutputToken()} list.
     * The size of list returned by this method is equal to the size of {@link PaginatorDefinition#getOutputToken()} list.
     */
    private List<String> fluentGetterMethodsForOutputToken() {
        return paginatorDefinition.getOutputToken().stream()
                                  .map(this::fluentGetterMethodForResponseMember)
                                  .collect(Collectors.toList());
    }

    /**
     * Returns the fluent getter method for a single member in the response.
     * The returned String includes the '()' after each method name.
     *
     * The input member can be a nested String. An example would be StreamDescription.LastEvaluatedShardId
     * which represents LastEvaluatedShardId member in StreamDescription class. The return value for it
     * would be "streamDescription().lastEvaluatedShardId()"
     *
     * @param member A top level or nested member in response of {@link #c2jOperationName}.
     */
    private String fluentGetterMethodForResponseMember(String member) {
        final String[] hierarchy = member.split("\\.");

        if (hierarchy.length < 1) {
            throw new IllegalArgumentException(String.format("Error when splitting member %s for operation %s",
                                                             member, c2jOperationName));
        }

        ShapeModel parentShape = operationModel.getOutputShape();
        final StringBuilder getterMethod = new StringBuilder();

        for (String str : hierarchy) {
            getterMethod.append(".")
                    .append(parentShape.findMemberModelByC2jName(str).getFluentGetterMethodName())
                    .append("()");

            parentShape =  parentShape.findMemberModelByC2jName(str).getShape();
        }

        return getterMethod.substring(1);
    }

    /**
     * @param input A top level or nested member in response of {@link #c2jOperationName}.
     *
     * @return The {@link MemberModel} of the {@link PaginatorDefinition#getResultKey()}. If input value is nested,
     * then member model of the last child shape is returned.
     *
     * For example, if input is StreamDescription.Shards, then the return value is "Shard" which is the member model for
     * the Shards.
     */
    private MemberModel memberModelForResponseMember(String input) {
        final String[] hierarchy = input.split("\\.");

        if (hierarchy.length < 1) {
            throw new IllegalArgumentException(String.format("Error when splitting value %s for operation %s",
                                                             input, c2jOperationName));
        }

        ShapeModel shape = operationModel.getOutputShape();

        for (int i = 0; i < hierarchy.length - 1; i++) {
            shape = shape.findMemberModelByC2jName(hierarchy[i]).getShape();
        }

        return shape.getMemberByC2jName(hierarchy[hierarchy.length - 1]);
    }

    /*
     * Returns the {@link TypeName} for a value in the {@link PaginatorDefinition#getResultKey()} list.
     *
     * Examples:
     * If paginated item is represented as List<String>, then member type is String.
     * If paginated item is represented as List<Foo>, then member type is Foo.
     * If paginated item is represented as Map<String, List<Foo>>,
     *              then member type is Map.Entry<String, List<Foo>>.
     */
    private TypeName getTypeForResultKey(String singleResultKey) {
        MemberModel resultKeyModel = memberModelForResponseMember(singleResultKey);

        if (resultKeyModel == null) {
            throw new InvalidParameterException("MemberModel is not found for result key: " + singleResultKey);
        }

        if (resultKeyModel.isList()) {
            return typeProvider.fieldType(resultKeyModel.getListModel().getListMemberModel());
        } else if (resultKeyModel.isMap()) {
            return typeProvider.mapEntryWithConcreteTypes(resultKeyModel.getMapModel());
        } else {
            throw new IllegalArgumentException(String.format("Key %s in paginated operation %s should be either a list or a map",
                                                             singleResultKey, c2jOperationName));
        }
    }

    /**
     * Generates a inner class that implements {@link NextPageSupplier}. An instance of this class
     * is passed to {@link PaginatedResponsesIterator} to be used while iterating through pages.
     */
    private TypeSpec nextPageSupplierClass() {
        return TypeSpec.classBuilder(nextPageSupplierClassName())
                       .addModifiers(Modifier.PRIVATE)
                       .addSuperinterface(ParameterizedTypeName.get(ClassName.get(NextPageSupplier.class), responseType()))
                       .addMethod(MethodSpec.methodBuilder(NEXT_PAGE_METHOD)
                                            .addModifiers(Modifier.PUBLIC)
                                            .addAnnotation(Override.class)
                                            .addParameter(responseType(), "currentPage")
                                            .returns(responseType())
                                            .addCode(nextPageMethodBody())
                                            .build())
                       .build();
    }

    /*
     * Returns {@link CodeBlock} for the NEXT_PAGE_METHOD.
     *
     * A sample from dynamoDB listTables paginator:
     *
     *  if (response == null || response.lastEvaluatedTableName() == null) {
     *      return null;
     *  } else {
     *      return client.listTables(firstRequest.toBuilder()
     *          .exclusiveStartTableName(response.lastEvaluatedTableName())
     *          .build());
     *  }
     *
     */
    private CodeBlock nextPageMethodBody() {
        return CodeBlock.builder()
                        .beginControlFlow(conditionForNextResponseNotExists())
                        .addStatement("return null")
                        .nextControlFlow("else")
                        .addStatement(codeToMakeNextServiceCall())
                        .endControlFlow()
                        .build();
    }

    private String conditionForNextResponseNotExists() {
        String controlFlow;

        if (paginatorDefinition.getMoreResults() != null) {
            controlFlow = String.format("if (currentPage == null || !currentPage.%s.booleanValue())",
                                        fluentGetterMethodForResponseMember(paginatorDefinition.getMoreResults()));
        } else {
            // If there is no more_results token, then output_token will be a single value
            controlFlow = String.format("if (currentPage == null || currentPage.%s == null)",
                                        fluentGetterMethodsForOutputToken().get(0));
        }

        return controlFlow;
    }

    /**
     * Generates the code to make next page call by using values from old response.
     *
     * Sample generated code:
     * return client.listTables(firstRequest.toBuilder().exclusiveStartTableName(response.lastEvaluatedTableName()).build());
     */
    private String codeToMakeNextServiceCall() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("return client.%s(%s.toBuilder()", operationModel.getMethodName(), REQUEST_MEMBER));

        List<String> requestSetterNames = fluentSetterMethodNamesForInputToken();
        List<String> responseGetterMethods = fluentGetterMethodsForOutputToken();

        for (int i = 0; i < paginatorDefinition.getInputToken().size(); i++) {
            sb.append(String.format(".%s(currentPage.%s)", requestSetterNames.get(i), responseGetterMethods.get(i)));
        }

        sb.append(".build())");

        return sb.toString();
    }

}
