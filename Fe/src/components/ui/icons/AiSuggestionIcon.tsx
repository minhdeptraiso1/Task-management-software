import React from 'react'

export interface AiSuggestionIconProps extends React.SVGProps<SVGSVGElement> {
  size?: number | string
  className?: string
  accentColor?: string
}

export const AiSuggestionIcon: React.FC<AiSuggestionIconProps> = ({
  size = 20,
  className = '',
  accentColor = '#FF1E27',
  ...props
}) => {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 100 100"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={`inline-block ${className}`}
      {...props}
    >
      {/* 5 Red Rays radiating from top-left lightbulb dome */}
      <rect x="5" y="44" width="12" height="4.5" rx="2.25" fill={accentColor} />
      <rect x="11" y="22" width="12" height="4.5" rx="2.25" transform="rotate(-35 11 22)" fill={accentColor} />
      <rect x="34" y="6" width="4.5" height="12" rx="2.25" fill={accentColor} />
      <rect x="55" y="16" width="12" height="4.5" rx="2.25" transform="rotate(35 55 16)" fill={accentColor} />
      <rect x="14" y="64" width="12" height="4.5" rx="2.25" transform="rotate(45 14 64)" fill={accentColor} />

      {/* Speech Bubble Contour */}
      <path
        d="M 44 42 H 82 C 87.5 42 92 46.5 92 52 V 76 C 92 81.5 87.5 86 82 86 H 58 L 48 95 V 86 H 44 C 38.5 86 34 81.5 34 76 V 72"
        stroke="currentColor"
        strokeWidth="6"
        strokeLinecap="round"
        strokeLinejoin="round"
      />

      {/* Text lines inside Speech Bubble */}
      <line x1="56" y1="54" x2="80" y2="54" stroke="currentColor" strokeWidth="5" strokeLinecap="round" />
      <line x1="50" y1="65" x2="82" y2="65" stroke="currentColor" strokeWidth="5" strokeLinecap="round" />
      <line x1="50" y1="76" x2="74" y2="76" stroke="currentColor" strokeWidth="5" strokeLinecap="round" />

      {/* Lightbulb Dome */}
      <path
        d="M 26 52 C 20 46 19 35 26 27 C 32 20 43 20 50 27 C 56 34 55 45 49 52 C 46 55 43 58 43 62 H 32 C 32 58 29 55 26 52 Z"
        stroke="currentColor"
        strokeWidth="6"
        strokeLinecap="round"
        strokeLinejoin="round"
        fill="white"
      />

      {/* Lightbulb Filaments */}
      <path
        d="M 34 50 C 34 42 37 39 37 36"
        stroke="currentColor"
        strokeWidth="3.5"
        strokeLinecap="round"
      />
      <path
        d="M 41 50 C 41 42 38 39 37 36"
        stroke="currentColor"
        strokeWidth="3.5"
        strokeLinecap="round"
      />

      {/* Base Cap of Lightbulb */}
      <path
        d="M 32 62 H 43 M 34 68 H 41 M 36 73 H 39"
        stroke="currentColor"
        strokeWidth="5"
        strokeLinecap="round"
      />

      {/* Connector from base to speech bubble */}
      <path
        d="M 37.5 73 V 86"
        stroke="currentColor"
        strokeWidth="6"
        strokeLinecap="round"
      />
    </svg>
  )
}
