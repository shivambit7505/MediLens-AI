import React, { useState, useEffect } from 'react';
import { providerApi } from '../api/providerApi';
import { CareNavigationResponse, Provider } from '../types';
import {
  Compass,
  UserCheck,
  Building,
  Phone,
  Star,
  MapPin,
  Video,
  CheckCircle,
  HelpCircle,
  Search,
  ArrowRight,
} from 'lucide-react';

export const CareNavigatorPage: React.FC = () => {
  const [data, setData] = useState<CareNavigationResponse | null>(null);
  const [providers, setProviders] = useState<Provider[]>([]);
  const [selectedSpecialty, setSelectedSpecialty] = useState<string>('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [telehealthOnly, setTelehealthOnly] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadCareNavigation();
  }, []);

  const loadCareNavigation = async () => {
    try {
      setLoading(true);
      const res = await providerApi.getCareNavigation();
      setData(res);
      setProviders(res.nearbyProviders);
    } catch (err) {
      console.error('Failed to load care navigation', err);
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = async (spec: string, query: string, teleOnly: boolean) => {
    setSelectedSpecialty(spec);
    setSearchQuery(query);
    setTelehealthOnly(teleOnly);

    try {
      const filtered = await providerApi.searchProviders({
        specialty: spec,
        query,
        telehealthOnly: teleOnly,
      });
      setProviders(filtered);
    } catch (err) {
      console.error('Failed to filter providers', err);
    }
  };

  if (loading) {
    return (
      <div className="max-w-6xl mx-auto py-16 text-center text-sm text-gray-500 flex flex-col items-center justify-center space-y-3">
        <Compass className="h-8 w-8 text-teal-600 animate-spin" />
        <span>Loading Clinical Care Navigator & Provider Directory...</span>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto space-y-8 pb-12">
      {/* Header */}
      <div>
        <div className="flex items-center space-x-3">
          <div className="p-2 bg-teal-100 dark:bg-teal-950 text-teal-600 dark:text-teal-400 rounded-lg">
            <Compass className="h-6 w-6" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
              Clinical Care Navigator & Specialist Directory
            </h1>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Personalized medical specialty routing derived directly from your diagnostic laboratory anomalies.
            </p>
          </div>
        </div>
      </div>

      {/* Specialty Recommendations Section */}
      {data && data.recommendations.length > 0 && (
        <div className="space-y-4">
          <h2 className="text-base font-bold text-gray-900 dark:text-white flex items-center">
            <UserCheck className="h-4 w-4 mr-2 text-teal-600" />
            Recommended Clinical Referrals (Based on Lab Findings)
          </h2>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {data.recommendations.map((rec, idx) => (
              <div
                key={idx}
                className="bg-white dark:bg-gray-800 rounded-xl p-5 shadow-sm border border-gray-200 dark:border-gray-700 flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-center justify-between mb-2">
                    <span className="font-bold text-base text-gray-900 dark:text-white">
                      {rec.specialty}
                    </span>
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase ${
                      rec.urgency === 'URGENT' ? 'bg-amber-100 text-amber-700 dark:bg-amber-950 dark:text-amber-400' : 'bg-teal-100 text-teal-700'
                    }`}>
                      {rec.urgency} CONSULT
                    </span>
                  </div>

                  <p className="text-xs text-gray-600 dark:text-gray-300 mb-3">
                    {rec.clinicalReason}
                  </p>

                  {rec.triggeringBiomarkers.length > 0 && (
                    <div className="mb-3">
                      <span className="text-[11px] font-semibold text-gray-500 uppercase tracking-wide block mb-1">
                        Triggering Lab Values:
                      </span>
                      <div className="flex flex-wrap gap-1.5">
                        {rec.triggeringBiomarkers.map((b, i) => (
                          <span
                            key={i}
                            className="px-2 py-0.5 bg-gray-100 dark:bg-gray-700 rounded text-[11px] text-gray-800 dark:text-gray-200"
                          >
                            {b}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}

                  <div className="p-3 bg-gray-50 dark:bg-gray-900/60 rounded-lg text-xs border border-gray-100 dark:border-gray-700">
                    <span className="font-semibold text-gray-800 dark:text-gray-200 flex items-center mb-1">
                      <HelpCircle className="h-3.5 w-3.5 mr-1 text-teal-600" />
                      Suggested Questions for your Specialist:
                    </span>
                    <p className="text-gray-600 dark:text-gray-400 text-[11px] italic">
                      "{rec.suggestedQuestionsForDoctor}"
                    </p>
                  </div>
                </div>

                <div className="mt-4 pt-3 border-t border-gray-100 dark:border-gray-700 flex justify-end">
                  <button
                    type="button"
                    onClick={() => handleFilterChange(rec.specialty, searchQuery, telehealthOnly)}
                    className="inline-flex items-center text-xs font-semibold text-teal-600 dark:text-teal-400 hover:underline"
                  >
                    View {rec.specialty} Specialists <ArrowRight className="h-3 w-3 ml-1" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Directory Filter Bar */}
      <div className="bg-white dark:bg-gray-800 rounded-xl p-4 shadow-sm border border-gray-200 dark:border-gray-700 space-y-3">
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          {/* Search Query */}
          <div className="relative">
            <Search className="h-4 w-4 absolute left-3 top-2.5 text-gray-400" />
            <input
              type="text"
              placeholder="Search doctor, clinic, city..."
              value={searchQuery}
              onChange={(e) => handleFilterChange(selectedSpecialty, e.target.value, telehealthOnly)}
              className="w-full pl-9 pr-3 py-1.5 text-xs rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-900 text-gray-900 dark:text-white"
            />
          </div>

          {/* Specialty Dropdown */}
          <div>
            <select
              value={selectedSpecialty}
              onChange={(e) => handleFilterChange(e.target.value, searchQuery, telehealthOnly)}
              className="w-full px-3 py-1.5 text-xs rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-900 text-gray-900 dark:text-white"
            >
              <option value="ALL">All Specialties</option>
              <option value="Endocrinology">Endocrinology</option>
              <option value="Cardiology">Cardiology</option>
              <option value="Nephrology">Nephrology</option>
              <option value="Hematology">Hematology</option>
              <option value="Internal Medicine">Internal Medicine / Primary Care</option>
            </select>
          </div>

          {/* Telehealth Toggle */}
          <div className="flex items-center pl-2">
            <input
              type="checkbox"
              id="telehealthToggle"
              checked={telehealthOnly}
              onChange={(e) => handleFilterChange(selectedSpecialty, searchQuery, e.target.checked)}
              className="h-4 w-4 text-teal-600 rounded border-gray-300 focus:ring-teal-500"
            />
            <label htmlFor="telehealthToggle" className="ml-2 text-xs font-medium text-gray-700 dark:text-gray-300 flex items-center">
              <Video className="h-3.5 w-3.5 mr-1 text-teal-600" />
              Telehealth Consultations Only
            </label>
          </div>
        </div>
      </div>

      {/* Providers Grid */}
      <div className="space-y-4">
        <h2 className="text-base font-bold text-gray-900 dark:text-white flex items-center justify-between">
          <span>Accredited Healthcare Providers ({providers.length})</span>
          <span className="text-xs font-normal text-gray-500">Verified Clinical Network</span>
        </h2>

        {providers.length === 0 ? (
          <div className="p-12 text-center bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700">
            <Compass className="h-10 w-10 text-gray-400 mx-auto mb-2" />
            <p className="text-sm font-semibold text-gray-700 dark:text-gray-300">
              No healthcare providers match your filters
            </p>
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
              Try adjusting your specialty or search query to find matching clinics.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {providers.map((p) => (
              <div
                key={p.id}
                className="bg-white dark:bg-gray-800 rounded-xl p-5 shadow-sm border border-gray-200 dark:border-gray-700 flex flex-col justify-between space-y-4 hover:shadow-md transition-shadow"
              >
                <div>
                  <div className="flex items-start justify-between">
                    <div>
                      <h3 className="font-bold text-sm text-gray-900 dark:text-white">
                        {p.name}
                      </h3>
                      <p className="text-xs text-teal-600 dark:text-teal-400 font-medium">
                        {p.title}
                      </p>
                    </div>
                    <div className="flex items-center text-xs font-bold text-amber-500 bg-amber-50 dark:bg-amber-950/60 px-2 py-0.5 rounded">
                      <Star className="h-3 w-3 fill-amber-400 mr-1" />
                      {p.rating}
                    </div>
                  </div>

                  <div className="mt-3 space-y-1.5 text-xs text-gray-600 dark:text-gray-300">
                    <div className="flex items-center text-gray-800 dark:text-gray-200 font-semibold">
                      <Building className="h-3.5 w-3.5 mr-1.5 text-gray-400" />
                      {p.clinicName}
                    </div>
                    <div className="flex items-center">
                      <MapPin className="h-3.5 w-3.5 mr-1.5 text-gray-400" />
                      {p.address}, {p.city}, {p.state} ({p.distanceMiles} miles)
                    </div>
                    <div className="flex items-center">
                      <Phone className="h-3.5 w-3.5 mr-1.5 text-gray-400" />
                      {p.phone}
                    </div>
                  </div>

                  <div className="mt-3 flex flex-wrap gap-1">
                    {p.telehealthAvailable && (
                      <span className="px-2 py-0.5 bg-blue-50 dark:bg-blue-950 text-blue-700 dark:text-blue-300 rounded text-[10px] font-semibold flex items-center">
                        <Video className="h-2.5 w-2.5 mr-1" /> Telehealth Available
                      </span>
                    )}
                    {p.acceptingNewPatients && (
                      <span className="px-2 py-0.5 bg-emerald-50 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-300 rounded text-[10px] font-semibold flex items-center">
                        <CheckCircle className="h-2.5 w-2.5 mr-1" /> Accepting Patients
                      </span>
                    )}
                  </div>
                </div>

                <div className="pt-3 border-t border-gray-100 dark:border-gray-700 flex gap-2">
                  <a
                    href={`tel:${p.phone.replace(/[^0-9]/g, '')}`}
                    className="flex-1 py-1.5 bg-teal-600 hover:bg-teal-700 text-white text-xs font-semibold rounded-lg text-center flex items-center justify-center"
                  >
                    <Phone className="h-3 w-3 mr-1" /> Call Clinic
                  </a>
                  <button
                    type="button"
                    onClick={() => alert(`Connecting with ${p.clinicName} appointment scheduling.`)}
                    className="flex-1 py-1.5 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 text-gray-800 dark:text-gray-200 text-xs font-semibold rounded-lg text-center"
                  >
                    Book Visit
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Statutory Disclaimer */}
      {data?.statutoryDisclaimer && (
        <div className="text-[11px] text-gray-400 dark:text-gray-500 italic p-3 bg-gray-50 dark:bg-gray-900/40 rounded-lg">
          {data.statutoryDisclaimer}
        </div>
      )}
    </div>
  );
};
