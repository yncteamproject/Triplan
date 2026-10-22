import type { ComponentPropsWithoutRef, ReactElement } from "react";

// Button.jsx의 props (타입스크립트 파일에서 쓸 때)
interface ButtonProps extends ComponentPropsWithoutRef<"button"> {
	variant?: "primary" | "secondary" | "text" | "danger";
	size?: "md" | "lg";
	fullWidth?: boolean;
	to?: string;
}

export default function Button(props: ButtonProps): ReactElement;