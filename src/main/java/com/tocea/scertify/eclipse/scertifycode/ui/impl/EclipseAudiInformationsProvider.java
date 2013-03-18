package com.tocea.scertify.eclipse.scertifycode.ui.impl;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.runtime.CoreException;

import com.tocea.scertify.eclipse.scertifycode.ui.api.IScertifyAuditInformationsProvider;

public class EclipseAudiInformationsProvider implements
		IScertifyAuditInformationsProvider {

	public IMarkerDelta[] findScertifyMarkerDeltas(IResourceChangeEvent event) {

		return event.findMarkerDeltas(getMarkerType(), true);
	}

	private String getMarkerType() {

		return IMarker.PROBLEM;
	}

	public IMarker[] findScertifyMarkers(IResource resource, int depth) {

		try {
			return resource.findMarkers(getMarkerType(), true, depth);
		} catch (final CoreException e) {
			return new IMarker[] {};
		}
	}

	public int getPreviousNumberOfViolations(int depth, IResource... resources) {

		return 0;
	}

}
