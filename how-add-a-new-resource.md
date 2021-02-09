# How to add a new resource

### Goals

- Learn about integration issues as soon as possible
- Get a working steel thread complete, then add on functionality

### Steps

1. Discovery
    1. Document required data and searches. This will be used to negotiate table structure and datamart model.
    1. Negotiate Datamart model with CDW team.
    1. Negotiate DB schema with CDW team.
    1. Negotiate new resource scope with Auth team.
1. Add datamart model support
    1. Add Datamart model to DQ.
    1. Update `DatamartValidationController` to support new type.
    1. **Deploy** to give validation access to CDW team.
1. Add synthetic data
    1. Add JPA entity and repository to DQ.
    1. Add test data to Synthetic DB.
    1. **Deploy** Synthetic DB updates
1. Add database support
    1. Add resource controller with readRaw support.
        1. Add ITs
    1. Update magic patient scopes to grant new resource type.
    1. Update deployment ingress/alb definitions.
    1. **Deploy** to allow testing CDW interaction.
1. Add read support
    1. Add ICN Majig to DQ.
    1. Create Transformer
    1. Add read support
        1. Add ITs
    1. **Deploy**
1. Add initial search support
    1. Add search by _id support
        1. Add bundling support
        1. Add ITs
    1. **Deploy**
1. Add search support, one at time. For each required search parameter
    1. Update resource controller to support parameter
    1. Add ITs
    1. **Deploy**
1. Add machine-to-machine integration support
    1. Update metadata
    1. Add crawler support
        1. Update `DataQueryScopes`
    1. **Deploy**
1. Publicize new resource
    1. Update OpenAPI in `DataQueryService`
    1. Update documentation prose
    1. **Deploy**
