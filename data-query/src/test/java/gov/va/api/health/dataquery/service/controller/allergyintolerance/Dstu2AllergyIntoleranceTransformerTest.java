package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.AllergyIntoleranceSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.AllergyIntoleranceSamples.Dstu2;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance.Category;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance.Certainty;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance.Status;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance.Type;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class Dstu2AllergyIntoleranceTransformerTest {
  @Test
  public void category() {
    assertThat(tx().category(null)).isNull();
    assertThat(tx().category(Category.medication))
        .isEqualTo(AllergyIntolerance.Category.medication);
    assertThat(tx().category(Category.food)).isEqualTo(AllergyIntolerance.Category.food);
  }

  @Test
  public void certainty() {
    assertThat(tx().certainty(null)).isNull();
    assertThat(tx().certainty(Certainty.unlikely)).isEqualTo(AllergyIntolerance.Certainty.unlikely);
    assertThat(tx().certainty(Certainty.likely)).isEqualTo(AllergyIntolerance.Certainty.likely);
    assertThat(tx().certainty(Certainty.confirmed))
        .isEqualTo(AllergyIntolerance.Certainty.confirmed);
  }

  @Test
  public void empty() {
    assertThat(
            Dstu2AllergyIntoleranceTransformer.builder()
                .datamart(DatamartAllergyIntolerance.builder().build())
                .build()
                .toFhir())
        .isEqualTo(AllergyIntolerance.builder().resourceType("AllergyIntolerance").build());
  }

  @Test
  public void manifestations() {
    assertThat(tx().manifestations(null)).isNull();
    assertThat(tx().manifestations(List.of())).isNull();
    assertThat(tx().manifestations(Datamart.create().manifestations()))
        .isEqualTo(Dstu2.create().manifestations());
  }

  @Test
  public void notes() {
    assertThat(tx().notes(null)).isNull();
    assertThat(tx().notes(List.of())).isNull();
    assertThat(tx().notes(Datamart.create().emptyNotes())).isNull();
    assertThat(tx().notes(Datamart.create().notes())).isEqualTo(Dstu2.create().note());
  }

  @Test
  public void reactions() {
    assertThat(tx().reactions(null)).isNull();
    assertThat(tx().reactions(Optional.empty())).isNull();
    assertThat(tx().reactions(Datamart.create().emptyReactions())).isNull();
    assertThat(tx().reactions(Datamart.create().reactions())).isEqualTo(Dstu2.create().reactions());
  }

  @Test
  public void status() {
    assertThat(tx().status(null)).isNull();
    assertThat(tx().status(Status.active)).isEqualTo(AllergyIntolerance.Status.active);
    assertThat(tx().status(Status.confirmed)).isEqualTo(AllergyIntolerance.Status.active);
    assertThat(tx().status(Status.unconfirmed)).isEqualTo(AllergyIntolerance.Status.unconfirmed);
    assertThat(tx().status(Status.inactive)).isEqualTo(AllergyIntolerance.Status.inactive);
    assertThat(tx().status(Status.resolved)).isEqualTo(AllergyIntolerance.Status.resolved);
    assertThat(tx().status(Status.refuted)).isEqualTo(AllergyIntolerance.Status.refuted);
    assertThat(tx().status(Status.entered_in_error))
        .isEqualTo(AllergyIntolerance.Status.entered_in_error);
  }

  @Test
  public void substance() {
    assertThat(tx().substance(Optional.empty())).isNull();
    assertThat(tx().substance(Datamart.create().emptySubstance())).isNull();
    assertThat(tx().substance(Datamart.create().fullyPopulatedSubstance()))
        .isEqualTo(Dstu2.create().fullyPopulatedSubstance());
  }

  @Test
  public void toFhir() {
    assertThat(tx().toFhir()).isEqualTo(Dstu2.create().allergyIntolerance());
  }

  private Dstu2AllergyIntoleranceTransformer tx() {
    return Dstu2AllergyIntoleranceTransformer.builder()
        .datamart(AllergyIntoleranceSamples.Datamart.create().allergyIntolerance())
        .build();
  }

  @Test
  public void type() {
    assertThat(tx().type(null)).isNull();
    assertThat(tx().type(Type.allergy)).isEqualTo(AllergyIntolerance.Type.allergy);
    assertThat(tx().type(Type.intolerance)).isEqualTo(AllergyIntolerance.Type.intolerance);
  }
}
