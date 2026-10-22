import type { ReactElement, ReactNode } from "react";

// EmptyState.jsx의 props (타입스크립트 파일에서 쓸 때)
interface EmptyStateProps {
	icon?: ReactNode;
	title: ReactNode;
	description?: ReactNode;
	action?: ReactNode;
}

export default function EmptyState(props: EmptyStateProps): ReactElement;