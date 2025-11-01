# Enhanced Text Analysis API Documentation

This document outlines the enhanced text analysis features available in the SatyaCheck API.

## Overview

The Enhanced Analysis API provides more sophisticated text analysis capabilities beyond basic fact-checking, including:

1. Comprehensive content analysis
2. Misinformation pattern detection
3. Content categorization
4. Topic extraction
5. Named entity recognition
6. Sentiment analysis

## API Endpoints

### Comprehensive Analysis

**Endpoint**: `POST /api/v1/enhanced-analysis/comprehensive`

**Description**: Performs a full-spectrum analysis on provided content, including fact-checking, content categorization, topic extraction, named entity recognition, and sentiment analysis.

**Parameters**:
- `content` (required): The text content to analyze
- `language` (optional, default: "en"): The language of the content

**Example Request**:
```http
POST /api/v1/enhanced-analysis/comprehensive
Content-Type: application/x-www-form-urlencoded

content=Climate change is causing rising sea levels and more extreme weather events.&language=en
```

**Example Response**:
```json
{
  "factCheckResult": {
    "verified": true,
    "confidenceScore": 0.92,
    "issues": [],
    "sourcesReferences": [
      "IPCC Sixth Assessment Report",
      "NASA Global Climate Change Portal",
      "National Oceanic and Atmospheric Administration (NOAA)"
    ],
    "explanation": "The statement is scientifically accurate. Multiple scientific bodies have documented rising sea levels and increased frequency/intensity of extreme weather events linked to climate change.",
    "recommendedActions": [
      "For more detailed information, visit climate.nasa.gov"
    ]
  },
  "contentCategory": {
    "primaryCategory": "SCIENCE",
    "subCategories": ["Climate", "Environment", "Weather"],
    "confidence": 0.95,
    "tags": ["climate change", "sea levels", "extreme weather", "environment"]
  },
  "extractedTopics": [
    {
      "topic": "Climate Change",
      "relevance": 0.98,
      "subtopics": ["Global Warming", "Sea Level Rise", "Extreme Weather"],
      "keywords": ["climate", "sea levels", "weather", "extreme", "change"]
    }
  ],
  "namedEntities": [],
  "sentimentScore": -0.1,
  "additionalContext": {
    "categoryConfidence": 0.95,
    "topicCount": 1,
    "entityCount": 0,
    "analysisType": "COMPREHENSIVE",
    "sentimentMagnitude": 0.3,
    "sentimentLanguage": "en"
  }
}
```

### Misinformation Analysis

**Endpoint**: `POST /api/v1/enhanced-analysis/misinformation`

**Description**: Analyzes content specifically for misinformation patterns, techniques, and risk assessment.

**Parameters**:
- `content` (required): The text content to analyze
- `language` (optional, default: "en"): The language of the content

**Example Request**:
```http
POST /api/v1/enhanced-analysis/misinformation
Content-Type: application/x-www-form-urlencoded

content=Scientists are hiding the truth about climate change. It's all a hoax to get more research funding.&language=en
```

**Example Response**:
```json
{
  "factCheckResult": {
    "verified": false,
    "confidenceScore": 0.87,
    "issues": [
      "Makes unsupported claim about scientific consensus",
      "Employs conspiracy theory framing",
      "Mischaracterizes scientific funding processes"
    ],
    "sourcesReferences": [
      "Scientific consensus on climate change (NASA)",
      "Peer review process in scientific journals",
      "Climate research funding sources"
    ],
    "explanation": "The statement makes unfounded claims about scientific dishonesty without evidence.",
    "recommendedActions": [
      "Review peer-reviewed scientific literature on climate change",
      "Learn about how scientific research funding works"
    ]
  },
  "misinformationPatterns": [
    "Conspiracy theory pattern claiming coordinated deception",
    "Appeal to hidden motives without evidence",
    "Rejection of scientific consensus without counter-evidence"
  ],
  "misinformationTechniques": [
    "Appeal to conspiracy",
    "Undermining expert credibility",
    "False motive attribution"
  ],
  "contentCategory": {
    "primaryCategory": "SCIENCE",
    "subCategories": ["Climate", "Misinformation"],
    "confidence": 0.89,
    "tags": ["climate change", "hoax", "conspiracy", "science funding"]
  },
  "extractedTopics": [
    {
      "topic": "Climate Change Denial",
      "relevance": 0.95,
      "subtopics": ["Scientific Conspiracy", "Funding Motivation", "Climate Hoax"],
      "keywords": ["hoax", "scientists", "funding", "climate", "hiding"]
    }
  ],
  "additionalContext": {
    "misinformationRisk": "HIGH",
    "techniquesIdentified": 3,
    "analysisType": "MISINFORMATION_FOCUSED"
  }
}
```

### URL Analysis (Coming Soon)

**Endpoint**: `POST /api/v1/enhanced-analysis/url`

**Description**: Analyzes content from a provided URL.

**Parameters**:
- `url` (required): The URL to analyze
- `language` (optional, default: "en"): Expected language of the content

**Note**: This endpoint is planned but not yet implemented.

### Admin Cache Management

**Endpoint**: `POST /api/admin/cache/clear/{cacheName}`

**Description**: Clears a specific cache by name.

**Parameters**:
- `cacheName` (path parameter): The name of the cache to clear

**Example Request**:
```http
POST /api/admin/cache/clear/enhancedAnalysis
```

**Endpoint**: `POST /api/admin/cache/clear-all`

**Description**: Clears all caches.

**Example Request**:
```http
POST /api/admin/cache/clear-all
```

## Error Handling

All API endpoints return appropriate HTTP status codes:

- `200 OK`: Request successful
- `400 Bad Request`: Invalid parameters
- `500 Internal Server Error`: Server-side error

Error responses include a JSON body with error details:

```json
{
  "status": "error",
  "message": "Error message details",
  "timestamp": "2023-06-01T12:34:56.789Z"
}
```

## Caching

Results from all analysis endpoints are cached to improve performance:

- Comprehensive analysis results: 1 hour
- Misinformation analysis results: 2 hours
- Content categories: 24 hours
- Extracted topics: 24 hours

Cache keys are based on content hash and language parameters.