package eu.dissco.dataexporter.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.junit.jupiter.api.Test;

class TargetTypeTest {

  @Test
  void testDigitalMediaFromString(){
    // When
    var result = TargetType.fromString(TargetType.DIGITAL_MEDIA.getName());

    // Then
    assertThat(result).isEqualTo(TargetType.DIGITAL_MEDIA);
  }

  @Test
  void testDigitalSpecimenFromString(){
    // When
    var result = TargetType.fromString(TargetType.DIGITAL_SPECIMEN.getName());

    // Then
    assertThat(result).isEqualTo(TargetType.DIGITAL_SPECIMEN);
  }

  @Test
  void testFromInvalidString() {
    // When / Then
    assertThrowsExactly(IllegalArgumentException.class, () -> TargetType.fromString("Invalid"));
  }


}
