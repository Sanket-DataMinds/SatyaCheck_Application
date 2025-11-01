# React/Web Version of TricolorShieldIcon

This document provides the implementation details for the TricolorShieldIcon component in the React/Web version of Satya Check.

## Component Specification

```tsx
import React from 'react';

interface TricolorShieldIconProps {
  className?: string;
  size?: number | string;
}

/**
 * A tricolor shield icon representing the Satya Check logo
 * Features:
 * - Shield shape with Indian flag colors (saffron, white, green)
 * - Navy blue checkmark in the center
 * - Dark grey border
 */
const TricolorShieldIcon: React.FC<TricolorShieldIconProps> = ({ 
  className = '', 
  size = 24 
}) => {
  // Convert size to pixels if it's a number
  const sizeStyle = typeof size === 'number' ? `${size}px` : size;

  return (
    <svg 
      width={sizeStyle} 
      height={sizeStyle} 
      viewBox="0 0 108 108" 
      fill="none" 
      xmlns="http://www.w3.org/2000/svg"
      className={className}
    >
      {/* Shield base with dark grey outline */}
      <path
        fill="#FFFFFF"
        stroke="#4A4A4A"
        strokeWidth="2"
        d="M54,20L20,30v28c0,23 15,39 34,50c19,-11 34,-27 34,-50V30L54,20z"
      />
        
      {/* Saffron/Orange top portion of the shield */}
      <path
        fill="#FF9933"
        d="M54,21L21,30.8v27.2h66V30.8L54,21z"
      />
        
      {/* White middle portion - not needed as fill but defined for clarity */}
      <path
        fill="#FFFFFF"
        d="M21,58h66v0h-66z"
      />
        
      {/* Green bottom portion of the shield */}
      <path
        fill="#138808"
        d="M21,58h66c0,22 -14.5,37.5 -33,48c-18.5,-10.5 -33,-26 -33,-48z"
      />
        
      {/* Navy blue checkmark in the center white band */}
      <path
        fill="#000080"
        d="M44,54l8,8l20,-20l-4,-4l-16,16l-4,-4z"
      />
    </svg>
  );
};

export default TricolorShieldIcon;
```

## Usage

The component can be used as follows:

```tsx
// Basic usage with default size (24px)
<TricolorShieldIcon />

// Custom size in pixels
<TricolorShieldIcon size={32} />

// Custom size with units
<TricolorShieldIcon size="2rem" />

// With additional CSS classes
<TricolorShieldIcon className="my-icon pulse-animation" />
```

## Animation Example

For the pulse animation effect, you can add this CSS to your stylesheets:

```css
@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

.pulse-animation {
  animation: pulse 2s infinite;
}
```

This provides the same visual appearance as the Android implementation, maintaining brand consistency across platforms.
