# SimQ - Face Quality Assessment

SimQ is an Android library for assessing the quality of face images. It analyzes multiple quality metrics to provide a comprehensive quality score between 0.0 and 1.0.

## Features

SimQ evaluates face images based on four key metrics:

- **Alignment**: Evaluates face pose angles (pitch, yaw, roll) and eye state
- **Blur**: Measures image sharpness using Laplacian variance
- **Brightness**: Assesses image luminance levels
- **Contrast**: Evaluates pixel intensity variation

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
implementation("com.simprints.biometrics:simq:2025.4.0")
```

## Basic Usage

```kotlin
// Initialize SimQ with default parameters
val simQ = SimQ()

// Calculate quality score for a face image
val qualityScore = simQ.calculateFaceQuality(
    bitmap = faceBitmap,
    pitch = 5.0,
    yaw = -3.0,
    roll = 0.0
)

// Quality score ranges from 0.0 (poor) to 1.0 (excellent)
if (qualityScore >= 0.6) {
    // Image quality is sufficient
}
```

## Advanced Usage

### Custom Weights

Customize how much each metric contributes to the final score:

```kotlin
val customWeights = QualityWeights(
    alignment = 0.25,
    blur = 0.35,
    brightness = 0.25,
    contrast = 0.10,
    eyeOpenness = 0.05
)

val simQ = SimQ(faceWeights = customWeights)
```

### Custom Parameters

Adjust the thresholds for each quality metric:

```kotlin
val customParameters = QualityParameters(
    maxAlignmentAngle = 15.0,
    minBlur = 60_000.0,
    maxBlur = 120_000.0,
    minBrightness = 40.0,
    optimalBrightnessLow = 90.0,
    optimalBrightnessHigh = 140.0,
    maxBrightness = 180.0,
    minContrast = 35.0,
    maxContrast = 50.0
)

val simQ = SimQ(faceParameters = customParameters)
```

## Parameters Reference

### calculateFaceQuality()

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `bitmap` | `Bitmap` | Required | The cropped face image |
| `pitch` | `Double` | `0.0` | Face pitch angle (head nod) in degrees |
| `yaw` | `Double` | `0.0` | Face yaw angle (head rotation) in degrees |
| `roll` | `Double` | `0.0` | Face roll angle (head tilt) in degrees |
| `leftEyeOpenness` | `Double?` | `null` | Left eye openness probability (0.0-1.0) |
| `rightEyeOpenness` | `Double?` | `null` | Right eye openness probability (0.0-1.0) |
| `centerCrop` | `Float` | `0.5f` | Fraction of image to analyze (0.0-1.0) |
| `horizontalDisplacement` | `Float` | `0.0f` | Horizontal shift for center crop (-1.0 to 1.0) |
| `verticalDisplacement` | `Float` | `0.0f` | Vertical shift for center crop (-1.0 to 1.0) |

### QualityWeights (Default Values)

| Weight | Default | Description |
|--------|---------|-------------|
| `alignment` | `0.28` | Face pose contribution |
| `blur` | `0.30` | Sharpness contribution |
| `brightness` | `0.30` | Luminance contribution |
| `contrast` | `0.10` | Contrast contribution |
| `eyeOpenness` | `0.02` | Eye state contribution |

### QualityParameters (Default Values)

| Parameter | Default | Description |
|-----------|---------|-------------|
| `maxAlignmentAngle` | `20.0` | Maximum combined angle deviation |
| `maxIndividualAngle` | `25.0` | Maximum single angle deviation |
| `minBlur` | `50,000.0` | Minimum acceptable Laplacian variance |
| `maxBlur` | `100,000.0` | Optimal Laplacian variance |
| `minBrightness` | `30.0` | Minimum acceptable brightness (0-255) |
| `optimalBrightnessLow` | `80.0` | Lower bound of optimal brightness |
| `optimalBrightnessHigh` | `150.0` | Upper bound of optimal brightness |
| `maxBrightness` | `190.0` | Maximum acceptable brightness |
| `brightnessSteepness` | `0.3` | Brightness scoring curve steepness |
| `minContrast` | `30.0` | Minimum acceptable contrast (std dev) |
| `maxContrast` | `47.0` | Optimal contrast (std dev) |
