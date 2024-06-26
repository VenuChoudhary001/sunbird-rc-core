import { RENDER_OUTPUT } from '../enums/renderOutput.enum';
import { W3CCredential } from 'vc.types';

export class RenderTemplateDTO {
  credential?: W3CCredential; // VC JSON
  credentialId?: string;
  schema: JSON; //SCHEMA JSON
  template: string; //TEMPLATE JSON
  output: RENDER_OUTPUT; //OUTPUT JSON
}
